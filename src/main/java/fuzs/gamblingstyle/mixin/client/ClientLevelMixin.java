package fuzs.gamblingstyle.mixin.client;

import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    @Shadow
    @Final
    private LevelRenderer levelRenderer;

    protected ClientLevelMixin(WritableLevelData p_204149_, ResourceKey<Level> p_204150_, Holder<DimensionType> p_204151_, Supplier<ProfilerFiller> p_204152_, boolean p_204153_, boolean p_204154_, long p_204155_) {
        super(p_204149_, p_204150_, p_204151_, p_204152_, p_204153_, p_204154_, p_204155_);
    }

    @Inject(method = "destroyBlockProgress", at = @At("TAIL"))
    public void destroyBlockProgress(int playerEntityId, BlockPos pos, int destroyProgress, CallbackInfo callbackInfo) {
        Entity entity = this.getEntity(playerEntityId);
        if (entity instanceof Player player && player.getMainHandItem().getItem() instanceof RangedDiggerItem rangedDiggerItem) {
            List<BlockPos> blocks = rangedDiggerItem.getAllHarvestBlocks(player.getMainHandItem(), pos, player, false).toList();
            for (int i = 0; i < blocks.size(); i++) {
                // there won't be this many entities, so we can safely use large entity ids
                // not sure if this messes with the size of the Int2ObjectMap, hopefully not haha
                this.levelRenderer.destroyBlockProgress(playerEntityId + i + Short.MAX_VALUE, blocks.get(i), destroyProgress);
            }
        }
    }
}
