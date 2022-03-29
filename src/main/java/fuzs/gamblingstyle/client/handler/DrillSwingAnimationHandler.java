package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.world.item.DrillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DrillSwingAnimationHandler {
    @SubscribeEvent
    public void onClickInput(InputEvent.ClickInputEvent evt) {
        if (evt.isAttack() && evt.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult.getType() == HitResult.Type.BLOCK && minecraft.player.getItemInHand(evt.getHand()).getItem() instanceof DrillItem) {
                evt.setSwingHand(false);
                // particles are also cancelled by disabling swing hand
                // check isDestroying to make sure this is the event called from continuous use method
                if (minecraft.gameMode.isDestroying()) {
                    BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
                    minecraft.particleEngine.addBlockHitEffects(hitResult.getBlockPos(), hitResult);
                }
            }
        }
    }
}
