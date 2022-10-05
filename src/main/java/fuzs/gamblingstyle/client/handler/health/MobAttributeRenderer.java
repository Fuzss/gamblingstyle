package fuzs.gamblingstyle.client.handler.health;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.LivingEntity;

public abstract class MobAttributeRenderer {
    private final int iconsPerRow;

    public MobAttributeRenderer(int iconsPerRow) {
        this.iconsPerRow = iconsPerRow;
    }

    public int getHeight() {
        return 11;
    }

    public abstract int getMaxValue(LivingEntity entity);

    public abstract int getValue(LivingEntity entity);

    public abstract boolean canRender(LivingEntity entity);

    public void renderIcons(LivingEntity entity, PoseStack poseStack, int posX, int posY, boolean withShadow) {
        if (!this.canRender(entity)) return;
        int value = this.getValue(entity);
        poseStack.pushPose();
        int icons = Math.min((int) Math.ceil(value / 4.0F) * 2, this.iconsPerRow);
        if (withShadow) {
            GuiComponent.fill(poseStack, posX - icons * 4 - 1, posY - 1, posX + icons * 4 + 2, posY + 9 + 1, Minecraft.getInstance().options.getBackgroundColor(0.25F));
            poseStack.translate(0.0F, 0.0F, 0.03F);
        }
        posX -= icons * 4;
        RenderSystem.enableDepthTest();
        for (int currentIcon = 0; currentIcon < icons; ++currentIcon) {

            int offsetX = posX + currentIcon * 8;
            if (currentIcon * 2 + 1 < value) {
                GuiComponent.blit(poseStack, offsetX, posY, 34, 9, 9, 9, 256, 256);
            }

            if (currentIcon * 2 + 1 == value) {
                GuiComponent.blit(poseStack, offsetX, posY, 25, 9, 9, 9, 256, 256);
            }

            if (currentIcon * 2 + 1 > value) {
                GuiComponent.blit(poseStack, offsetX, posY, 16, 9, 9, 9, 256, 256);
            }
        }
        RenderSystem.disableDepthTest();
        poseStack.popPose();
    }
}
