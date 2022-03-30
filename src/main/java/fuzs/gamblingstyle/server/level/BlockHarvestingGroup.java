package fuzs.gamblingstyle.server.level;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.proxy.Proxy;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockHarvestingGroup {
    private final Level level;
    private final Player player;
    public final BlockPos destroyBlockPos;
    private final Int2ObjectMap<BlockPos> areaBlocks = new Int2ObjectOpenHashMap<>();
    public ItemStack destroyingItem;
    private float destroyProgress;

    public BlockHarvestingGroup(Level level, Player player, BlockPos destroyBlockPos, Direction blockFace) {
        this.level = level;
        this.player = player;
        this.destroyBlockPos = destroyBlockPos;
        this.destroyingItem = player.getMainHandItem();
        this.buildBlockGroup(player, destroyBlockPos, blockFace);
    }

    private void buildBlockGroup(Player player, BlockPos centerBlock, Direction blockFace) {
        if (this.destroyingItem.getItem() instanceof RangedDiggerItem item) {
            List<BlockPos> blocks = item.getAllHarvestBlocks(this.destroyingItem, centerBlock, player, blockFace, false, true).toList();
            int playerEntityId = player.getId();
            for (int i = 0; i < blocks.size(); i++) {
                // there won't be this many entities, so we can safely use large entity ids
                // not sure if this messes with the size of the Int2ObjectMap, hopefully not haha
                this.areaBlocks.put(playerEntityId + i + Short.MAX_VALUE, blocks.get(i));
            }
        } else {
            throw new IllegalStateException("harvesting groups can only be created for area digger items");
        }
    }

    public void incrementDestroyProgress() {
        this.validateAreaBlocks();
        BlockState blockstate = this.level.getBlockState(this.destroyBlockPos);
        this.destroyProgress += blockstate.getDestroyProgress(this.player, this.level, this.destroyBlockPos);
        if (this.destroyProgress < 1.0F) {
            this.setDestroyProgress((int) (this.destroyProgress * 10.0F) - 1);
        }
    }

    public boolean tryDestroyBlocks() {
        if (this.destroyProgress >= 1.0F) {
            if (this.validateItem()) {
                this.areaBlocks.values().forEach(pos -> Proxy.INSTANCE.destroyBlock(this.level, pos, this.player));
            } else {
                GamblingStyle.LOGGER.warn("trying to destroy blocks with invalid item");
            }
            return true;
        }
        return false;
    }

    public void clearDestroyProgress() {
        this.setDestroyProgress(-1);
    }

    private void setDestroyProgress(int destroyProgress) {
        for (Int2ObjectMap.Entry<BlockPos> entry : this.areaBlocks.int2ObjectEntrySet()) {
            this.level.destroyBlockProgress(entry.getIntKey(), entry.getValue(), destroyProgress);
        }
    }

    private boolean validateItem() {
        ItemStack itemstack = this.player.getMainHandItem();
        boolean flag = ItemStack.isSameIgnoreDurability(itemstack, this.destroyingItem);
        flag &= !this.destroyingItem.shouldCauseBlockBreakReset(itemstack);
        return flag;
    }

    private void validateAreaBlocks() {
        ObjectIterator<Int2ObjectMap.Entry<BlockPos>> iterator = this.areaBlocks.int2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<BlockPos> entry = iterator.next();
            BlockPos pos = entry.getValue();
            if (!this.canHarvestBlockEfficiently(pos)) {
                this.level.destroyBlockProgress(entry.getIntKey(), pos, -1);
                iterator.remove();
            }
        }
    }

    private boolean canHarvestBlockEfficiently(BlockPos pos) {
        // first check if tool is effective (block might still be able to break with drops, but we don't want that case)
        // second check is if we will get drops
        BlockState state = this.level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (this.destroyingItem.getDestroySpeed(state) > 1.0F) {
            return state.canHarvestBlock(this.level, pos, this.player);
        }
        return false;
    }
}
