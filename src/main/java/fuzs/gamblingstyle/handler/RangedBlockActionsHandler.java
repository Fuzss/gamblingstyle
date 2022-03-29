package fuzs.gamblingstyle.handler;

import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.registry.ModRegistry;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class RangedBlockActionsHandler {
    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        evt.getPlayer().getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY).ifPresent(capability -> {
            capability.setLastHitBlockData(evt.getPos(), evt.getFace());
        });
    }

    @SubscribeEvent
    public void onBreakSpeed(final PlayerEvent.BreakSpeed evt) {
        if (evt.getPos() == null) return;
        Player player = evt.getPlayer();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof RangedDiggerItem rangedDiggerItem) {
            Level level = player.level;
            BlockPos pos = evt.getPos();
            LazyOptional<LastHitBlockCapability> optional = player.getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY);
            if (optional.isPresent() && rangedDiggerItem.canHarvestBlockEfficiently(stack, level, pos, player)) {
                LastHitBlockCapability capability = optional.orElseThrow(IllegalStateException::new);
                if (capability.isDataValid(pos)) {
                    List<BlockPos> blocks = RangedDiggerItem.getNeighboringBlocksOnAxis(pos, rangedDiggerItem.getAdditionalBlockRange(stack), capability.getLastHitBlockDirection().getAxis(), true);
                    double destroySpeed = level.getBlockState(pos).getDestroySpeed(level, pos);
                    double averageDestroySpeed = blocks.stream().filter(indirectPos -> rangedDiggerItem.canHarvestBlockEfficiently(stack, level, indirectPos, player))
                            .mapToDouble(indirectPos -> level.getBlockState(indirectPos).getDestroySpeed(level, indirectPos))
                            .max()
                            .orElse(destroySpeed);
                    // set the hardest block speed from the blocks mined plus malus of 20%
                    float multiplier = Math.min(1.0F, (float) (destroySpeed / averageDestroySpeed)) * 0.8F;
                    evt.setNewSpeed(evt.getNewSpeed() * multiplier);
                }
            }
        }
    }
}
