package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.server.level.BlockHarvestingGroup;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockHarvestingHandler {
    private final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private BlockHarvestingGroup harvestingGroup;

    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        Player player = evt.getPlayer();
        if (!(player instanceof AbstractClientPlayer) || this.minecraft.gameMode.getPlayerMode().isCreative()) return;
        if (player.getMainHandItem().getItem() instanceof RangedDiggerItem) {
            if (!this.minecraft.gameMode.isDestroying() || !this.sameDestroyTarget(evt.getPos())) {
                this.clearHarvestingGroup();
                this.harvestingGroup = new BlockHarvestingGroup(this.minecraft.level, player, evt.getPos(), evt.getFace());
            } else if (this.harvestingGroup == null) {
                GamblingStyle.LOGGER.warn("harvest group is null");
            } else {
                this.harvestingGroup.incrementDestroyProgress(evt.getPos(), evt.getFace());
                if (this.harvestingGroup.tryDestroyBlocks()) {
                    this.clearHarvestingGroup();
                }
            }
        } else {
            this.clearHarvestingGroup();
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        if (this.minecraft.gameMode == null) return;
        if (this.harvestingGroup != null && !this.minecraft.gameMode.isDestroying()) {
            this.clearHarvestingGroup();
        }
    }

    private void clearHarvestingGroup() {
        if (this.harvestingGroup != null) {
            this.harvestingGroup.clearAreaBlocks();
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
    @SubscribeEvent
    public void onClickInput(InputEvent.ClickInputEvent evt) {
        if (evt.isAttack() && evt.getHand() == InteractionHand.MAIN_HAND) {
            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                if (this.minecraft.player.getMainHandItem().getItem() instanceof RangedDiggerItem) {
                    evt.setSwingHand(false);
                    // particles are also cancelled by disabling swing hand, so we add them for all blocks (also center)
                    // check isDestroying to make sure this is the event called from continuous use method
                    if (this.minecraft.gameMode.isDestroying() && this.harvestingGroup != null) {
                        BlockHitResult hitResult = (BlockHitResult) this.minecraft.hitResult;
                        this.harvestingGroup.verifyHarvestingData(hitResult.getBlockPos(), hitResult.getDirection());
                        this.harvestingGroup.getAllAreaBlocks().forEach(pos -> {
                            this.minecraft.particleEngine.addBlockHitEffects(pos, hitResult.withPosition(pos).withDirection(this.harvestingGroup.getBlockFace()));
                        });
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(final PlaySoundEvent evt) {
        if (this.minecraft.level != null && this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            if (this.minecraft.player.getMainHandItem().getItem() instanceof RangedDiggerItem) {
                Level level = this.minecraft.level;
                SoundInstance soundInstance = evt.getOriginalSound();
                BlockHitResult hitResult = (BlockHitResult) this.minecraft.hitResult;
                BlockPos pos = hitResult.getBlockPos();
                if (isSameBlockSoundInstance(soundInstance, pos, level, this.minecraft.player) && this.harvestingGroup != null) {
                    this.harvestingGroup.verifyHarvestingData(hitResult.getBlockPos(), hitResult.getDirection());
                    List<BlockPos> blocks = this.harvestingGroup.getAllAreaBlocks().toList();
                    blocks.stream()
                            .skip(level.random.nextInt(blocks.size()))
                            .findAny()
                            .ifPresent(pos1 -> {
                                // get a random sound from all the blocks we are harvesting and play that instead of the clicked block's sound
                                SoundType soundtype1 = level.getBlockState(pos1).getSoundType(level, pos1, this.minecraft.player);
                                SimpleSoundInstance soundInstance1 = new SimpleSoundInstance(soundtype1.getHitSound(), SoundSource.BLOCKS, (soundtype1.getVolume() + 1.0F) / 8.0F, soundtype1.getPitch() * 0.5F, pos1);
                                evt.setSound(soundInstance1);
                            });
                }
            }
        }
    }

    private static boolean isSameBlockSoundInstance(SoundInstance soundInstance, BlockPos pos, Level level, Player player) {
        SoundType soundtype = level.getBlockState(pos).getSoundType(level, pos, player);
        if (soundInstance.getSource() == SoundSource.BLOCKS && soundInstance.getLocation().equals(soundtype.getHitSound().getLocation())) {
            return soundInstance.getX() == pos.getX() + 0.5 && soundInstance.getY() == pos.getY() + 0.5 && soundInstance.getZ() == pos.getZ() + 0.5;
        }
        return false;
    }
}
