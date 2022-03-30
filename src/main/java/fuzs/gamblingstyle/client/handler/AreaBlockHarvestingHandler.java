package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.server.level.BlockHarvestingGroup;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class AreaBlockHarvestingHandler {
    private final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private BlockHarvestingGroup harvestingGroup;

    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        Player player = evt.getPlayer();
        if (!(player instanceof AbstractClientPlayer) || this.minecraft.gameMode.getPlayerMode().isCreative()) return;
        if (player.getMainHandItem().getItem() instanceof RangedDiggerItem) {
            if (!this.minecraft.gameMode.isDestroying() || !this.sameDestroyTarget(evt.getPos())) {
                this.harvestingGroup = new BlockHarvestingGroup(this.minecraft.level, player, evt.getPos(), evt.getFace());
            } else {
                if (this.harvestingGroup == null) {
                    GamblingStyle.LOGGER.warn("harvest group is null");
                } else {
                    this.harvestingGroup.incrementDestroyProgress();
                    if (this.harvestingGroup.tryDestroyBlocks()) {
                        this.clearHarvestingGroup();
                    }
                }
            }
        } else {
            this.clearHarvestingGroup();
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent evt) {
        if (this.minecraft.gameMode == null) return;
        if (this.harvestingGroup != null && !this.minecraft.gameMode.isDestroying()) {
            this.clearHarvestingGroup();
        }
    }

    private void clearHarvestingGroup() {
        if (this.harvestingGroup != null) {
            this.harvestingGroup.clearDestroyProgress();
            this.harvestingGroup = null;
        }
    }

    private boolean sameDestroyTarget(BlockPos pos) {
        if (this.harvestingGroup == null) return false;
        ItemStack itemstack = this.minecraft.player.getMainHandItem();
        boolean flag = this.harvestingGroup.destroyingItem.isEmpty() && itemstack.isEmpty();
        if (!this.harvestingGroup.destroyingItem.isEmpty() && !itemstack.isEmpty()) {
            flag = !this.harvestingGroup.destroyingItem.shouldCauseBlockBreakReset(itemstack);
        }
        return pos.equals(this.harvestingGroup.destroyBlockPos) && flag;
    }
}
