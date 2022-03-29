package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.gamblingstyle.mixin.client.accessor.LevelRendererAccessor;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RangedItemVisualsHandler {
    @SubscribeEvent
    public void onClickInput(InputEvent.ClickInputEvent evt) {
        if (evt.isAttack() && evt.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK && minecraft.player.getItemInHand(evt.getHand()).getItem() instanceof RangedDiggerItem rangedDiggerItem) {
                evt.setSwingHand(false);
                // particles are also cancelled by disabling swing hand
                // check isDestroying to make sure this is the event called from continuous use method
                if (minecraft.gameMode.isDestroying()) {
                    BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
                    rangedDiggerItem.getAllHarvestBlocks(minecraft.player.getItemInHand(evt.getHand()), hitResult.getBlockPos(), minecraft.player, hitResult.getDirection(), true, true).forEach(pos -> {
                        minecraft.particleEngine.addBlockHitEffects(pos, hitResult.withPosition(pos));
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public void onHighlightBlock(final DrawSelectionEvent.HighlightBlock evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && evt.getCamera().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof RangedDiggerItem rangedDiggerItem) {
            Vec3 position = evt.getCamera().getPosition();
            rangedDiggerItem.getAllHarvestBlocks(player.getMainHandItem(), evt.getTarget().getBlockPos(), player, evt.getTarget().getDirection(), false, true).forEach(pos1 -> {
                BlockState state = minecraft.level.getBlockState(pos1);
                if (!state.isAir() && minecraft.level.getWorldBorder().isWithinBounds(pos1)) {
                    VertexConsumer vertexConsumer = evt.getMultiBufferSource().getBuffer(RenderType.lines());
                    ((LevelRendererAccessor) evt.getLevelRenderer()).callRenderHitOutline(evt.getPoseStack(), vertexConsumer, player, position.x(), position.y(), position.z(), pos1, state);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlaySound(final PlaySoundEvent evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            SoundInstance soundInstance = evt.getOriginalSound();
            BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
            BlockPos pos = hitResult.getBlockPos();
            SoundType soundtype = minecraft.level.getBlockState(pos).getSoundType(minecraft.level, pos, minecraft.player);
            if (soundInstance.getSource() == SoundSource.BLOCKS && soundInstance.getLocation().equals(soundtype.getHitSound().getLocation())) {
                if (soundInstance.getX() == pos.getX() + 0.5 && soundInstance.getY() == pos.getY() + 0.5 && soundInstance.getZ() == pos.getZ() + 0.5) {
                    if (minecraft.player.getMainHandItem().getItem() instanceof RangedDiggerItem rangedDiggerItem) {
                        rangedDiggerItem.getAllHarvestBlocks(minecraft.player.getMainHandItem(), pos, minecraft.player, true)
                                // get random stream element
                                .reduce((o1, o2) -> minecraft.player.getRandom().nextBoolean() ? o2 : o1)
                                .ifPresent(pos1 -> {
                                    // get a random sound from all the blocks we are harvesting and play that instead of the clicked block's sound
                                    SoundType soundtype1 = minecraft.level.getBlockState(pos1).getSoundType(minecraft.level, pos1, minecraft.player);
                                    SimpleSoundInstance soundInstance1 = new SimpleSoundInstance(soundtype1.getHitSound(), SoundSource.BLOCKS, (soundtype1.getVolume() + 1.0F) / 8.0F, soundtype1.getPitch() * 0.5F, pos1);
                                    evt.setSound(soundInstance1);
                                });
                    }
                }
            }
        }
    }
}
