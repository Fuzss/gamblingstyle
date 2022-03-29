package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.gamblingstyle.mixin.client.accessor.LevelRendererAccessor;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class RangedItemVisualsHandler {
    @SubscribeEvent
    public void onClickInput(InputEvent.ClickInputEvent evt) {
        if (evt.isAttack() && evt.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult.getType() == HitResult.Type.BLOCK && minecraft.player.getItemInHand(evt.getHand()).getItem() instanceof RangedDiggerItem) {
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

    @SubscribeEvent
    public void onHighlightBlock(final DrawSelectionEvent.HighlightBlock evt) {
        if (evt.getCamera().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof RangedDiggerItem rangedDiggerItem) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                BlockPos pos = evt.getTarget().getBlockPos();
                ItemStack stack = player.getMainHandItem();
                if (rangedDiggerItem.canHarvestBlockEfficiently(stack, minecraft.level, pos, player)) {
                    Vec3 position = evt.getCamera().getPosition();
                    List<BlockPos> blocks = RangedDiggerItem.getNeighboringBlocksOnAxis(pos, rangedDiggerItem.getAdditionalBlockRange(stack), evt.getTarget().getDirection().getAxis());
                    for (BlockPos indirectPos : blocks) {
                        if (rangedDiggerItem.canHarvestBlockEfficiently(stack, minecraft.level, indirectPos, player)) {
                            BlockState state = minecraft.level.getBlockState(indirectPos);
                            if (!state.isAir() && minecraft.level.getWorldBorder().isWithinBounds(indirectPos)) {
                                VertexConsumer vertexConsumer = evt.getMultiBufferSource().getBuffer(RenderType.lines());
                                ((LevelRendererAccessor) evt.getLevelRenderer()).callRenderHitOutline(evt.getPoseStack(), vertexConsumer, player, position.x(), position.y(), position.z(), indirectPos, state);
                            }
                        }
                    }
                }
            }
        }
    }
}
