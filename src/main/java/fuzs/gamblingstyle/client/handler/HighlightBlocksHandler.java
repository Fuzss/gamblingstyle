package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.gamblingstyle.mixin.client.accessor.LevelRendererAccessor;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class HighlightBlocksHandler {
    private BlockPos blockPosHighlightCache = BlockPos.ZERO;
    private Block blockHighlightCache;
    private List<BlockPos> blocksHighlightCache;

    @SubscribeEvent
    public void onHighlightBlock(final DrawSelectionEvent.HighlightBlock evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && evt.getCamera().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof RangedDiggerItem item) {
            this.tryUpdateHighlightCache(minecraft.level, player, evt.getTarget(), item);
            Vec3 position = evt.getCamera().getPosition();
            for (BlockPos pos : this.blocksHighlightCache) {
                BlockState state = minecraft.level.getBlockState(pos);
                if (!state.isAir() && minecraft.level.getWorldBorder().isWithinBounds(pos)) {
                    VertexConsumer vertexConsumer = evt.getMultiBufferSource().getBuffer(RenderType.lines());
                    ((LevelRendererAccessor) evt.getLevelRenderer()).callRenderHitOutline(evt.getPoseStack(), vertexConsumer, player, position.x(), position.y(), position.z(), pos, state);
                }
            }
        }
    }

    private void tryUpdateHighlightCache(Level level, Player player, BlockHitResult hitResult, RangedDiggerItem item) {
        BlockPos highlightPos = hitResult.getBlockPos();
        Direction highlightFace = hitResult.getDirection();
        if (!highlightPos.equals(this.blockPosHighlightCache) || this.blocksHighlightCache == null || level.getBlockState(highlightPos).getBlock() != this.blockHighlightCache) {
            this.blockPosHighlightCache = highlightPos;
            this.blockHighlightCache = level.getBlockState(highlightPos).getBlock();
            this.blocksHighlightCache = item.getAllHarvestBlocks(player.getMainHandItem(), highlightPos, player, highlightFace, false, true).toList();
        }
    }
}
