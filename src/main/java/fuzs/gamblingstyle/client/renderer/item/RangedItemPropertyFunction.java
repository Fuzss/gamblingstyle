package fuzs.gamblingstyle.client.renderer.item;

import fuzs.gamblingstyle.mixin.client.accessor.ItemInHandRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class RangedItemPropertyFunction implements ClampedItemPropertyFunction {
    private final Minecraft minecraft = Minecraft.getInstance();
    private int destroyingBlockCooldown;

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;
        if (this.destroyingBlockCooldown > 0) {
            this.destroyingBlockCooldown--;
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        this.destroyingBlockCooldown = 8;
    }

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int index) {
        if (entity != null && entity == this.minecraft.player) {
            // we only want to compare with main hand item, but main hand item in ItemInHandRenderer is only updated on the next tick,
            // the item is rendered before that though resulting in the non-active texture briefly flashing
            if (this.destroyingBlockCooldown > 0 && (stack == entity.getMainHandItem() || stack == ((ItemInHandRendererAccessor) this.minecraft.getItemInHandRenderer()).getMainHandItem())) {
                return 1.0F;
            }
        }
        return 0.0F;
    }
}
