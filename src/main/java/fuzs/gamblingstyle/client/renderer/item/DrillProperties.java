package fuzs.gamblingstyle.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class DrillProperties implements ClampedItemPropertyFunction {
    private final Minecraft minecraft = Minecraft.getInstance();
    private int destroyingBlockCooldown;

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;
        if (this.minecraft.gameMode != null) {
            if (this.minecraft.gameMode.isDestroying()) {
                this.destroyingBlockCooldown = 8;
            } else if (this.destroyingBlockCooldown > 0) {
                this.destroyingBlockCooldown--;
            }
        }
    }

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int index) {
        if (entity != null && entity == this.minecraft.player) {
            if (this.destroyingBlockCooldown > 0 && entity.getMainHandItem() == stack) {
                return 1.0F;
            }
        }
        return 0.0F;
    }
}
