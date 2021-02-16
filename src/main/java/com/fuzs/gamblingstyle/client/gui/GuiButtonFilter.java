package com.fuzs.gamblingstyle.client.gui;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.client.gui.core.ITooltipButton;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class GuiButtonFilter extends GuiButton implements ITooltipButton {

    private static final ResourceLocation FILTER_BUTTON = new ResourceLocation(GamblingStyle.MODID, "textures/gui/container/merchant_filter.png");

    private ITradingInfo.FilterMode filterMode;

    public GuiButtonFilter(int buttonId, int x, int y, ITradingInfo.FilterMode filterMode) {

        super(buttonId, x, y, 10, 10, "");
        this.filterMode = filterMode;
    }

    @Override
    public void setPosition(int posX, int posY) {

        this.x = posX;
        this.y = posY;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        if (this.visible) {

            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            mc.getTextureManager().bindTexture(FILTER_BUTTON);
            GlStateManager.disableDepth();

            final int buttonSize = 10;
            int textureX = buttonSize * this.filterMode.ordinal();
            int textureY = buttonSize;
            if (this.hovered) {

                textureY += buttonSize;
            }

            this.drawTexturedModalRect(this.x, this.y, textureX, textureY, this.width, this.height);
            GlStateManager.enableDepth();
        }
    }

    @Override
    public List<String> getToolTip(GuiScreen screen, int mouseX, int mouseY) {

        ITextComponent tooltipComponent = new TextComponentTranslation("gui.button.show", new TextComponentTranslation(this.filterMode.key));
        return Lists.newArrayList(tooltipComponent.getUnformattedText());
    }

    public void cycleFilterMode(boolean skipFavorites) {

        int nextIndex = (this.filterMode.ordinal() + 1) % ITradingInfo.FilterMode.values().length;
        this.filterMode = ITradingInfo.FilterMode.values()[nextIndex];
        if (skipFavorites && this.filterMode == ITradingInfo.FilterMode.FAVORITES) {

            this.cycleFilterMode(false);
        }
    }

    public ITradingInfo.FilterMode getFilterMode() {

        return this.filterMode;
    }

}