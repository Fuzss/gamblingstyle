package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameplateEvent;

import java.util.Random;

public class PetHealthRenderer {
    public static final PetHealthRenderer INSTANCE = new PetHealthRenderer();
    private final int heartsPerRow = 5;
    private final int armorPlatesPerRow = 5;
    private final Random random = new Random();
    protected int tickCount;

    public void onClientTick$End() {
        this.tickCount++;
    }

    public void onRenderNameplate(RenderNameplateEvent evt) {
        if (evt.getEntity() instanceof TamableAnimal tamableAnimal && tamableAnimal.isOwnedBy(Minecraft.getInstance().player) || evt.getEntity() instanceof AbstractHorse abstractHorse && Minecraft.getInstance().player.getUUID().equals(abstractHorse.getOwnerUUID())) {
            LivingEntity pet = (LivingEntity) evt.getEntity();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (false && !this.shouldShowName(pet, entityRenderDispatcher)) return;
            PoseStack poseStack = evt.getPoseStack();
            poseStack.pushPose();
            float f = pet.getBbHeight() + 0.5F;
            int offsetY = "deadmau5".equals(evt.getContent().getString()) ? 0 : 19 + 2;
            poseStack.translate(0.0D, f, 0.0D);
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.0125F, -0.0125F, 0.0125F);
            int healthHeight = this.renderPlayerHealth(poseStack, pet, 0, offsetY, true);
            this.renderMobArmor(poseStack, pet, 0, offsetY + healthHeight, true);
            poseStack.popPose();
        }
    }

    protected boolean shouldShowName(LivingEntity entity, EntityRenderDispatcher entityRenderDispatcher) {
        if (entity != entityRenderDispatcher.crosshairPickEntity) {
            return false;
        }
        double d0 = entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isDiscrete() ? 32.0F : 64.0F;
        if (d0 >= (double) (f * f)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            boolean flag = !entity.isInvisibleTo(localplayer);
            if (entity != localplayer) {
                Team team = entity.getTeam();
                Team team1 = localplayer.getTeam();
                if (team != null) {
                    Team.Visibility team$visibility = team.getNameTagVisibility();
                    switch (team$visibility) {
                        case ALWAYS:
                            return flag;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                        case HIDE_FOR_OWN_TEAM:
                            return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && flag && !entity.isVehicle();
        }
    }

    public int renderPlayerHealth(PoseStack poseStack, LivingEntity entity, int posX, int posY, boolean withShadow) {
        if (entity != null) {
            poseStack.pushPose();
            int currentHealth = Mth.ceil(entity.getHealth());
            this.random.setSeed(this.tickCount * 312871L);
            float maxHealth = Math.max((float) entity.getAttributeValue(Attributes.MAX_HEALTH), currentHealth);
            // absorption is only synced to the client for player's, so this doesn't work
            int currentAbsorption = Mth.ceil(entity.getAbsorptionAmount());
            float maxHealthWithAbsorption = maxHealth + currentAbsorption;
            int healthRows = Mth.ceil(maxHealthWithAbsorption / 2.0F / (float) this.heartsPerRow) ;
            int healthRowOffset = Math.max(10 - (healthRows - 2), 3);
            int regenerationOffset = -1;
            if (entity.hasEffect(MobEffects.REGENERATION)) {
                regenerationOffset = this.tickCount % Mth.ceil(maxHealth + 5.0F);
            }
            int fullHealthRowsHeight = Math.min(9, healthRowOffset) * (int) (maxHealthWithAbsorption / 2.0F / (float) this.heartsPerRow) + Math.max(0, 9 - healthRowOffset);
            if (withShadow) {
                boolean fullRows = (int) (maxHealthWithAbsorption / 2.0F) / this.heartsPerRow > 0;
                boolean unfilledRow = (int) (maxHealthWithAbsorption / 2.0F) % this.heartsPerRow > 0;
                if (fullRows) {
                    int displayWidth = Math.min(this.heartsPerRow, (int) Math.ceil(maxHealthWithAbsorption / 2.0F)) * 8 + 1;
                    GuiComponent.fill(poseStack, posX - displayWidth / 2 - 1, posY - 1, posX + displayWidth / 2 + 2, posY + fullHealthRowsHeight + 1 + (unfilledRow ? 0 : 1), Minecraft.getInstance().options.getBackgroundColor(0.25F));
                }
                if (unfilledRow) {
                    int lastRowDisplayWidth = ((int) Math.ceil(maxHealthWithAbsorption / 2.0F)) % this.heartsPerRow * 8;
                    GuiComponent.fill(poseStack, posX - lastRowDisplayWidth / 2 - 1, posY + fullHealthRowsHeight + 1 + (fullRows ? 0 : -2), posX + lastRowDisplayWidth / 2 + 2, posY + fullHealthRowsHeight + healthRowOffset + (fullRows ? 1 : -1), Minecraft.getInstance().options.getBackgroundColor(0.25F));
                }
                poseStack.translate(0.0F, 0.0F, 0.03F);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.enableDepthTest();
            this.renderHearts(poseStack, entity, posX, posY, healthRowOffset, regenerationOffset, maxHealth, currentHealth, currentAbsorption);
            RenderSystem.disableDepthTest();
            poseStack.popPose();
            return fullHealthRowsHeight + healthRowOffset + 2;
        }
        return 0;
    }

    public void renderMobArmor(PoseStack poseStack, LivingEntity player, int posX, int posY, boolean withShadow) {
        int armorValue = player.getArmorValue();
        if (armorValue > 0) {
            poseStack.pushPose();
            int armorPlates = Math.min((int) Math.ceil(armorValue / 4.0F) * 2, this.armorPlatesPerRow);
            if (withShadow) {
                GuiComponent.fill(poseStack, posX - armorPlates * 4 - 1, posY - 1, posX + armorPlates * 4 + 2, posY + 9 + 1, Minecraft.getInstance().options.getBackgroundColor(0.25F));
                poseStack.translate(0.0F, 0.0F, 0.03F);
            }
            posX -= armorPlates * 4;
            RenderSystem.enableDepthTest();
            for (int currentArmorPlate = 0; currentArmorPlate < armorPlates; ++currentArmorPlate) {

                int offsetX = posX + currentArmorPlate * 8;
                if (currentArmorPlate * 2 + 1 < armorValue) {
                    GuiComponent.blit(poseStack, offsetX, posY, 34, 9, 9, 9, 256, 256);
                }

                if (currentArmorPlate * 2 + 1 == armorValue) {
                    GuiComponent.blit(poseStack, offsetX, posY, 25, 9, 9, 9, 256, 256);
                }

                if (currentArmorPlate * 2 + 1 > armorValue) {
                    GuiComponent.blit(poseStack, offsetX, posY, 16, 9, 9, 9, 256, 256);
                }
            }
            RenderSystem.disableDepthTest();
            poseStack.popPose();
        }
    }

    protected void renderHearts(PoseStack poseStack, LivingEntity entity, int posX, int posY, int healthRowOffset, int regenerationOffset, float maxHealth, int currentHealth, int currentAbsorption) {
        HeartType gui$hearttype = HeartType.forPlayer(entity);
        int textureOffsetY = 9 * (entity.level.getLevelData().isHardcore() ? 5 : 0);
        int normalHearts = Mth.ceil(maxHealth / 2.0D);
        int absorptionHearts = Mth.ceil(currentAbsorption / 2.0D);
        int halfHearts = normalHearts * 2;

        poseStack.translate(0.0F, 0.0F, (Math.ceil((maxHealth + currentAbsorption) / (float) this.heartsPerRow) - 1.0F) * 0.03F);

        for (int currentHeart = normalHearts + absorptionHearts - 1; currentHeart >= 0; --currentHeart) {
            int heartRow = currentHeart / this.heartsPerRow;
            int heartColumn = currentHeart % this.heartsPerRow;
            if (heartColumn == this.heartsPerRow - 1) {
                poseStack.translate(0.0F, 0.0F, -0.03F);
            }
            int heartPosX = posX + heartColumn * 8;
            int heartsInCurrentRow;
            if (currentHeart < (normalHearts + absorptionHearts) / this.heartsPerRow * this.heartsPerRow) {
                heartsInCurrentRow = this.heartsPerRow;
            } else {
                heartsInCurrentRow = (normalHearts + absorptionHearts) % this.heartsPerRow;
            }
            heartPosX -= (heartsInCurrentRow * 8 + 1) / 2;
            int heartPosY = posY + heartRow * healthRowOffset;
            if (currentHealth + currentAbsorption <= 4) {
                heartPosY += this.random.nextInt(2);
            }

            if (currentHeart < normalHearts && currentHeart == regenerationOffset) {
                heartPosY -= 2;
            }

            this.renderHeart(poseStack, HeartType.CONTAINER, heartPosX, heartPosY, textureOffsetY, false);
            int j2 = currentHeart * 2;
            boolean absorptionHeart = currentHeart >= normalHearts;
            if (absorptionHeart) {
                int k2 = j2 - halfHearts;
                if (k2 < currentAbsorption) {
                    boolean flag1 = k2 + 1 == currentAbsorption;
                    this.renderHeart(poseStack, gui$hearttype == HeartType.WITHERED ? gui$hearttype : HeartType.ABSORBING, heartPosX, heartPosY, textureOffsetY, flag1);
                }
            }

            if (j2 < currentHealth) {
                boolean flag3 = j2 + 1 == currentHealth;
                this.renderHeart(poseStack, gui$hearttype, heartPosX, heartPosY, textureOffsetY, flag3);
            }
        }
    }

    private void renderHeart(PoseStack poseStack, HeartType heartType, int posX, int posY, int textureY, boolean halfHeart) {
        GuiComponent.blit(poseStack, posX, posY, heartType.getX(halfHeart, false), textureY, 9, 9, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(0, false), NORMAL(2, true), POISIONED(4, true), WITHERED(6, true), ABSORBING(8, false), FROZEN(9, false);

        private final int index;
        private final boolean canBlink;

        private HeartType(int p_168729_, boolean p_168730_) {
            this.index = p_168729_;
            this.canBlink = p_168730_;
        }

        public int getX(boolean p_168735_, boolean p_168736_) {
            int i;
            if (this == CONTAINER) {
                i = p_168736_ ? 1 : 0;
            } else {
                int j = p_168735_ ? 1 : 0;
                int k = this.canBlink && p_168736_ ? 2 : 0;
                i = j + k;
            }

            return 16 + (this.index * 2 + i) * 9;
        }

        static HeartType forPlayer(LivingEntity entity) {
            HeartType gui$hearttype;
            if (entity.hasEffect(MobEffects.POISON)) {
                gui$hearttype = POISIONED;
            } else if (entity.hasEffect(MobEffects.WITHER)) {
                gui$hearttype = WITHERED;
            } else if (entity.isFullyFrozen()) {
                gui$hearttype = FROZEN;
            } else {
                gui$hearttype = NORMAL;
            }

            return gui$hearttype;
        }
    }
}
