package fuzs.gamblingstyle.world.item;

import com.google.common.collect.ImmutableSet;
import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.proxy.Proxy;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.Set;

public class DrillItem extends DiggerItem {
    public static final Set<ToolAction> DEFAULT_DRILL_ACTIONS = ImmutableSet.of(ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG);

    public DrillItem(Tier p_204110_, float p_204108_, float p_204109_, Properties p_204112_) {
        super(p_204108_, p_204109_, p_204110_, ModRegistry.MINEABLE_WITH_DRILL_TAG, p_204112_);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return DEFAULT_DRILL_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSame(oldStack, newStack);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        LazyOptional<LastHitBlockCapability> optional = player.getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY);
        if (optional.isPresent()) {
            LastHitBlockCapability capability = optional.orElseThrow(IllegalStateException::new);
            Level level = player.level;
            if (capability.isDataValid(pos) && canHarvestBlockIndirectly(level, pos, itemstack, player)) {
                List<BlockPos> blocks = getNeighboringBlocksOnAxis(pos, 1, capability.getAndClearData().getAxis());
                for (BlockPos indirectPos : blocks) {
                    if (canHarvestBlockIndirectly(level, indirectPos, itemstack, player)) {
                        Proxy.INSTANCE.destroyBlock(level, indirectPos, player);
                    }
                }
            }
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    public static boolean canHarvestBlockIndirectly(Level level, BlockPos pos, ItemStack stack, Player player) {
        // first check if tool is effective (block might still be able to break with drops, but we don't want that case)
        // second check is if we will get drops
        BlockState state = level.getBlockState(pos);
        return stack.getDestroySpeed(state) > 1.0F && state.canHarvestBlock(level, pos, player);
    }

    private static List<BlockPos> getNeighboringBlocksOnAxis(BlockPos pos, int distance, Direction.Axis axis) {
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
            return axis.choose(pos1.getX(), pos1.getY(), pos1.getZ()) == axisCoordinate && !pos1.equals(pos);
        }).map(BlockPos::immutable).toList();
    }
}
