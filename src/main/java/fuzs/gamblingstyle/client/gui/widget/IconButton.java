package fuzs.gamblingstyle.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * a copy of {@link net.minecraft.client.gui.components.ImageButton} with mutable texture coordinates
 */
public class IconButton extends Button {
    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;
    protected int xTexStart;
    protected int yTexStart;

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, resourceLocation, 256, 256, onPress);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, resourceLocation, textureWidth, textureHeight, onPress, TextComponent.EMPTY);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress, Component component) {
        this(x, y, width, height, xTexStart, yTexStart, resourceLocation, textureWidth, textureHeight, onPress, NO_TOOLTIP, component);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, OnPress onPress, OnTooltip onTooltip) {
        this(x, y, width, height, xTexStart, yTexStart, resourceLocation, 256, 256, onPress, onTooltip, TextComponent.EMPTY);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress, OnTooltip onTooltip, Component component) {
        super(x, y, width, height, component, onPress, onTooltip);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.resourceLocation = resourceLocation;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTexture(int textureX, int textureY) {
        this.xTexStart = textureX;
        this.yTexStart = textureY;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderButtonBackground(poseStack, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        blit(poseStack, this.x, this.y, this.xTexStart, this.yTexStart + this.getYImage(this.isHoveredOrFocused()) * this.textureHeight, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    protected void renderButtonBackground(PoseStack poseStack, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        this.blit(poseStack, this.x, this.y, 0, 46 + this.getYImage(this.isHoveredOrFocused()) * 20, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + this.getYImage(this.isHoveredOrFocused()) * 20, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
    }
}
