package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.gamblingstyle.mixin.client.accessor.LevelRendererAccessor;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HighlightBlocksHandler {
    @SubscribeEvent
    public void onHighlightBlock(final DrawSelectionEvent.HighlightBlock evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && evt.getCamera().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof RangedDiggerItem item) {
            Vec3 position = evt.getCamera().getPosition();
            item.getAllHarvestBlocks(player.getMainHandItem(), evt.getTarget().getBlockPos(), player, evt.getTarget().getDirection(), false, true).forEach(pos1 -> {
                BlockState state = minecraft.level.getBlockState(pos1);
                if (!state.isAir() && minecraft.level.getWorldBorder().isWithinBounds(pos1)) {
                    VertexConsumer vertexConsumer = evt.getMultiBufferSource().getBuffer(RenderType.lines());
                    ((LevelRendererAccessor) evt.getLevelRenderer()).callRenderHitOutline(evt.getPoseStack(), vertexConsumer, player, position.x(), position.y(), position.z(), pos1, state);
                }
            });
        }
    }
}
