package fuzs.gamblingstyle.handler;

import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HitBlockFaceHandler {
    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        evt.getPlayer().getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY).ifPresent(capability -> {
            capability.setLastHitBlockData(evt.getPos(), evt.getFace());
        });
    }
}
