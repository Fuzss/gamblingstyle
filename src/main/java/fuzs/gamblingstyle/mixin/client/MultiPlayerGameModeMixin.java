package fuzs.gamblingstyle.mixin.client;

import fuzs.gamblingstyle.api.event.world.BlockBrokenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.AFTER), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;STOP_DESTROY_BLOCK:Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;")))
    public void continueDestroyBlock$fieldStopDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new BlockBrokenEvent(this.minecraft.level, pos, this.minecraft.player, direction));
    }
}
