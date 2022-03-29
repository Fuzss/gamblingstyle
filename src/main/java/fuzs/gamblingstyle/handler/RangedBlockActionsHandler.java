package fuzs.gamblingstyle.handler;

import fuzs.gamblingstyle.registry.ModRegistry;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RangedBlockActionsHandler {
    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        // storing this data should be rather important as it is the block face being hit sent by the client
        // recalculating this on the server might lead to a different result already when there is a minor desync
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
            double destroySpeed = level.getBlockState(pos).getDestroySpeed(level, pos);
            double averageDestroySpeed = rangedDiggerItem.getAllHarvestBlocks(stack, pos, player, true)
                    .mapToDouble(pos1 -> level.getBlockState(pos1).getDestroySpeed(level, pos1))
                    .max()
                    .orElse(destroySpeed);
            // set the hardest block speed from the blocks mined plus malus of 20%
            float multiplier = Math.min(1.0F, (float) (destroySpeed / averageDestroySpeed)) * 0.8F;
            evt.setNewSpeed(evt.getNewSpeed() * multiplier);
        }
    }
}
