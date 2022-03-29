package fuzs.gamblingstyle.world.item;

import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.proxy.Proxy;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.stream.Stream;

public abstract class RangedDiggerItem extends DiggerItem {
    public RangedDiggerItem(float baseAttackDamage, float attackSpeed, Tier tier, TagKey<Block> blocks, Properties properties) {
        super(baseAttackDamage, attackSpeed, tier, blocks, properties);
    }

    @Override
    public abstract boolean canPerformAction(ItemStack stack, ToolAction toolAction);

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSame(oldStack, newStack);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        this.getAllHarvestBlocks(stack, pos, player).forEach(pos1 -> Proxy.INSTANCE.destroyBlock(player.level, pos1, player));
        return super.onBlockStartBreak(stack, pos, player);
    }

    public Stream<BlockPos> getAllHarvestBlocks(ItemStack stack, BlockPos pos, Player player) {
        LazyOptional<LastHitBlockCapability> optional = player.getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY);
        if (optional.isPresent()) {
            LastHitBlockCapability capability = optional.orElseThrow(IllegalStateException::new);
            if (capability.isDataValid(pos) && this.canHarvestBlockEfficiently(stack, player.level, pos, player)) {
                List<BlockPos> blocks = getNeighboringBlocksOnAxis(pos, this.getAdditionalBlockRange(stack), capability.getLastHitBlockDirection().getAxis());
                return blocks.stream().filter(pos1 -> this.canHarvestBlockEfficiently(stack, player.level, pos1, player));
            }
        }
        return Stream.empty();
    }

    public int getAdditionalBlockRange(ItemStack stack) {
        return 1;
    }

    public boolean canHarvestBlockEfficiently(ItemStack stack, Level level, BlockPos pos, Player player) {
        // first check if tool is effective (block might still be able to break with drops, but we don't want that case)
        // second check is if we will get drops
        BlockState state = level.getBlockState(pos);
        return stack.getDestroySpeed(state) > 1.0F && state.canHarvestBlock(level, pos, player);
    }

    public static List<BlockPos> getNeighboringBlocksOnAxis(BlockPos pos, int distance, Direction.Axis axis) {
        return getNeighboringBlocksOnAxis(pos, distance, axis, false);
    }

    public static List<BlockPos> getNeighboringBlocksOnAxis(BlockPos pos, int distance, Direction.Axis axis, boolean all) {
        int minHeight;
        int maxHeight;
        if (axis != Direction.Axis.Y) {
            minHeight = pos.getY() - Math.min(1, distance);
            maxHeight = pos.getY() + 2 * distance - Math.min(1, distance);
        } else {
            minHeight = pos.getY() - distance;
            maxHeight = pos.getY() + distance;
        }
        int axisCoordinate = axis.choose(pos.getX(), pos.getY(), pos.getZ());
        return BlockPos.betweenClosedStream(pos.getX() - distance, minHeight, pos.getZ() - distance, pos.getX() + distance, maxHeight, pos.getZ() + distance).filter(pos1 -> {
            return axis.choose(pos1.getX(), pos1.getY(), pos1.getZ()) == axisCoordinate && (all || !pos1.equals(pos));
        }).map(BlockPos::immutable).toList();
    }
}
