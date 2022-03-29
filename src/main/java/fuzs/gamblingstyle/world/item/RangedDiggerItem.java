package fuzs.gamblingstyle.world.item;

import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.proxy.Proxy;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

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
        Stream<BlockPos> blocksToHarvest = this.getAllHarvestBlocks(stack, pos, player, false);
        Stream<BlockPos> blocksToUpdate = this.getAllHarvestBlocks(stack, pos, player, null, false, false);
        blocksToHarvest.forEach(pos1 -> Proxy.INSTANCE.destroyBlock(player.level, pos1, player));
        if (player instanceof ServerPlayer serverPlayer) {
            blocksToUpdate.forEach(pos1 -> serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverPlayer.level, pos)));
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    public Stream<BlockPos> getAllHarvestBlocks(ItemStack stack, BlockPos pos, Player player, boolean allBlocks) {
        return this.getAllHarvestBlocks(stack, pos, player, null, allBlocks, true);
    }

    public Stream<BlockPos> getAllHarvestBlocks(ItemStack stack, BlockPos pos, Player player, @Nullable Direction lastHitBlockDirection, boolean allBlocks, boolean applyFilter) {
        if (lastHitBlockDirection == null) {
            lastHitBlockDirection = this.getLastHitBlockDirection(player, pos);
        }
        if (lastHitBlockDirection != null && this.canHarvestBlockEfficiently(stack, player.level, pos, player)) {
            List<BlockPos> blocks = getNeighboringBlocksOnAxis(pos, this.getAdditionalBlockRange(stack), lastHitBlockDirection.getAxis(), allBlocks);
            Stream<BlockPos> stream = blocks.stream();
            return applyFilter ? stream.filter(pos1 -> this.canHarvestBlockEfficiently(stack, player.level, pos1, player)) : stream;
        }
        return Stream.empty();
    }

    @Nullable
    private Direction getLastHitBlockDirection(Player player, BlockPos pos) {
        LazyOptional<LastHitBlockCapability> optional = player.getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY);
        if (optional.isPresent()) {
            LastHitBlockCapability capability = optional.orElseThrow(IllegalStateException::new);
            if (capability.isDataValid(pos)) {
                return capability.getLastHitBlockDirection();
            }
        }
        return null;
    }

    public int getAdditionalBlockRange(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.POTENCY_ENCHANTMENT.get(), stack);
    }

    private boolean canHarvestBlockEfficiently(ItemStack stack, Level level, BlockPos pos, Player player) {
        // first check if tool is effective (block might still be able to break with drops, but we don't want that case)
        // second check is if we will get drops
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && stack.getDestroySpeed(state) > 1.0F && state.canHarvestBlock(level, pos, player);
    }

    private static List<BlockPos> getNeighboringBlocksOnAxis(BlockPos pos, int distance, Direction.Axis axis, boolean all) {
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
