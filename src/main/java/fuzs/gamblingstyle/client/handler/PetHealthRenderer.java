package fuzs.gamblingstyle.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameplateEvent;

import java.util.Random;

public class PetHealthRenderer {
    public static final PetHealthRenderer INSTANCE = new PetHealthRenderer();
    private final Random random = new Random();
    protected int tickCount;

    public void onClientTick$End() {
        this.tickCount++;
    }

    public void onRenderNameplate(RenderNameplateEvent evt) {
        if (evt.getEntity() instanceof TamableAnimal || evt.getEntity() instanceof AbstractHorse) {
            LivingEntity pet = (LivingEntity) evt.getEntity();
            PoseStack poseStack = evt.getPoseStack();
            poseStack.pushPose();
            float f = pet.getBbHeight() + 0.5F;
            int offsetY = "deadmau5".equals(evt.getContent().getString()) ? -20 : -10;
            poseStack.translate(0.0D, f, 0.0D);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.0125F, -0.0125F, 0.0125F);
            int currentHealth = Mth.ceil(pet.getHealth());
            float maxHealth = Math.max((float) pet.getAttributeValue(Attributes.MAX_HEALTH), currentHealth);
//            GuiComponent.fill(poseStack, (int) (-maxHealth / 4.0F * 9.0F) - 1, offsetY - 10 - 1, (int) (maxHealth / 4.0F * 9.0F) + 1, offsetY, Minecraft.getInstance().options.getBackgroundColor(Integer.MIN_VALUE));
//            poseStack.translate(0.0F, 0.0F, 0.03F);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
//            RenderSystem.enableDepthTest();
            this.renderPlayerHealth(poseStack, pet, (int) -(maxHealth * 2.0F + 1), offsetY, true);
//            RenderSystem.disableDepthTest();
            poseStack.popPose();
        }
    }

    public void renderPlayerHealth(PoseStack poseStack, LivingEntity entity, int posX, int posY, boolean withShadow) {
        if (entity != null) {
            int currentHealth = Mth.ceil(entity.getHealth());
            this.random.setSeed(this.tickCount * 312871L);
            float maxHealth = Math.max((float) entity.getAttributeValue(Attributes.MAX_HEALTH), currentHealth);
            // absorption is only synced to the client for player's, so this doesn't work
            int currentAbsorption = Mth.ceil(entity.getAbsorptionAmount());
            int healthRows = Mth.ceil((maxHealth + currentAbsorption) / 2.0F / 10.0F);
            int firstHealthRowOffset = Math.max(10 - (healthRows - 2), 3);
            int regenerationOffset = -1;
            if (entity.hasEffect(MobEffects.REGENERATION)) {
                regenerationOffset = this.tickCount % Mth.ceil(maxHealth + 5.0F);
            }
            int displayWidth = Math.min(10, (int) Math.ceil((maxHealth + currentAbsorption) / 2.0F)) * 8 + 1;
            posX -= displayWidth / 2;
            if (withShadow) {
                GuiComponent.fill(poseStack, posX - 1, posY - 1, posX + displayWidth + 1, posY + 10, Minecraft.getInstance().options.getBackgroundColor(Integer.MIN_VALUE));
                poseStack.translate(0.0F, 0.0F, 0.03F);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.enableDepthTest();
            this.renderHearts(poseStack, entity, posX, posY, firstHealthRowOffset, regenerationOffset, maxHealth, currentHealth, currentAbsorption);
            RenderSystem.disableDepthTest();
        }
    }

    protected void renderHearts(PoseStack poseStack, LivingEntity entity, int posX, int posY, int firstHealthRowOffset, int regenerationOffset, float maxHealth, int currentHealth, int currentAbsorption) {
        HeartType gui$hearttype = HeartType.forPlayer(entity);
        int i = 9 * (entity.level.getLevelData().isHardcore() ? 5 : 0);
        int j = Mth.ceil(maxHealth / 2.0D);
        int k = Mth.ceil(currentAbsorption / 2.0D);
        int l = j * 2;

        for (int i1 = j + k - 1; i1 >= 0; --i1) {
            int j1 = i1 / 10;
            int k1 = i1 % 10;
            int l1 = posX + k1 * 8;
            int i2 = posY - j1 * firstHealthRowOffset;
            if (currentHealth + currentAbsorption <= 4) {
                i2 += this.random.nextInt(2);
            }

            if (i1 < j && i1 == regenerationOffset) {
                i2 -= 2;
            }

            this.renderHeart(poseStack, HeartType.CONTAINER, l1, i2, i, false);
            int j2 = i1 * 2;
            boolean flag = i1 >= j;
            if (flag) {
                int k2 = j2 - l;
                if (k2 < currentAbsorption) {
                    boolean flag1 = k2 + 1 == currentAbsorption;
                    this.renderHeart(poseStack, gui$hearttype == HeartType.WITHERED ? gui$hearttype : HeartType.ABSORBING, l1, i2, i, flag1);
                }
            }

            if (j2 < currentHealth) {
                boolean flag3 = j2 + 1 == currentHealth;
                this.renderHeart(poseStack, gui$hearttype, l1, i2, i, flag3);
            }
        }
    }

    private void renderHeart(PoseStack poseStack, HeartType heartType, int posX, int posY, int textureY, boolean halfHeart) {
        GuiComponent.blit(poseStack, posX, posY, heartType.getX(halfHeart, false), textureY, 9, 9, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISIONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        FROZEN(9, false);

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
