package com.fuzs.gamblingstyle.client.gui;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.client.gui.core.ITooltipButton;
import com.fuzs.gamblingstyle.client.gui.data.TradingRecipe;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiButtonTradingRecipe extends GuiButton implements ITooltipButton {

    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation(GamblingStyle.MODID, "textures/gui/container/merchant_book.png");

    private final ItemStack[] recipe = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private int recipeId;
    private boolean isSelectedRecipe;
    private boolean hasContents;
    private boolean soldOut;
    private boolean favorite;

    public GuiButtonTradingRecipe(int id, int posX, int posY) {

        super(id, posX, posY, 84, 22, "");
        this.visible = false;
    }

    public void setContents(int id, TradingRecipe recipe, boolean soldOut) {

        this.recipeId = id;
        this.recipe[0] = recipe.getItemToBuy();
        this.recipe[1] = recipe.getSecondItemToBuy();
        this.recipe[2] = recipe.getItemToSell();
        this.isSelectedRecipe = recipe.isSelected();
        this.hasContents = recipe.hasRecipeContents();
        this.soldOut = soldOut;
        this.favorite = recipe.isFavorite();
    }

    public int getRecipeId() {

        return this.recipeId;
    }

    @Override
    public void setPosition(int posX, int posY) {

        this.x = posX;
        this.y = posY;
    }

    public boolean mousePressedOnFavorite(int mouseX, int mouseY) {

        final int buttonSize = 9;
        boolean pressed = this.enabled && this.visible && mouseX >= this.x - 3 && mouseY >= this.y + 6 && mouseX < this.x - 3 + buttonSize && mouseY < this.y + 6 + buttonSize;
        if (pressed) {

            this.favorite = !this.favorite;
        }

        return pressed;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        if (this.visible) {

            final int favoriteButtonSize = 9;
            boolean isFavoriteHovered = mouseX >= this.x - 3 && mouseY >= this.y + 6 && mouseX < this.x - 3 + favoriteButtonSize && mouseY < this.y + 6 + favoriteButtonSize;
            this.hovered = !isFavoriteHovered && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            mc.getTextureManager().bindTexture(RECIPE_BOOK);

            // draw background frame
            this.drawTexturedModalRect(this.x, this.y, 112, this.getTextureY(), this.width, this.height);
            if (this.soldOut) {

                this.drawTexturedModalRect(this.x + 47, this.y + 3, this.hasContents ? 0 : 10, 166, 10, 15);
            }

            // draw favorite button
            this.drawTexturedModalRect(this.x - 3, this.y + 6, this.favorite ? 20 : 29, 166, favoriteButtonSize, favoriteButtonSize);

            // draw items
            this.renderItemAndEffect(mc, this.recipe[0], this.x + 6, this.y + 2);
            this.renderItemAndEffect(mc, this.recipe[2], this.x + 61, this.y + 2);
            if (!this.recipe[1].isEmpty()) {

                this.renderItemAndEffect(mc, this.recipe[1], this.x + 27, this.y + 2);
            }

            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
        }
    }

    private int getTextureY() {

        int textureY = 0;
        if (this.hovered) {

            textureY += 22;
        }

        if (!this.hasContents) {

            textureY += 88;
        }

        if (this.isSelectedRecipe) {

            textureY += 44;
        }

        return textureY;
    }

    private void renderItemAndEffect(Minecraft mc, ItemStack itemStack, int xPosition, int yPosition) {

        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, xPosition, yPosition);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, xPosition, yPosition);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public List<String> getToolTip(GuiScreen screen, int mouseX, int mouseY) {

        ItemStack itemstack = this.getItemStackInRegion(mouseX, mouseY);
        List<String> list = Lists.newArrayList();
        if (!itemstack.isEmpty()) {

            list = screen.getItemToolTip(itemstack);
        } else if (this.soldOut && this.isPointInRegion(47, 3, 10, 15, mouseX, mouseY)) {

            list.add(new TextComponentTranslation("merchant.deprecated").getUnformattedText());
        }

        // helps with Quark which adds a blank line for drawing icons which are never drawn in the trading menu
        if (!list.isEmpty() && TextFormatting.getTextWithoutFormattingCodes(list.get(list.size() - 1).trim()).isEmpty()) {

            list.remove(list.size() - 1);
        }

        return list;
    }

    private ItemStack getItemStackInRegion(int mouseX, int mouseY) {

        if (this.isPointInRegion(6, 2, 16, 16, mouseX, mouseY)) {

            return this.recipe[0];
        } else if (this.isPointInRegion(27, 2, 16, 16, mouseX, mouseY)) {

            return this.recipe[1];
        } else if (this.isPointInRegion(61, 2, 16, 16, mouseX, mouseY)) {

            return this.recipe[2];
        }

        return ItemStack.EMPTY;
    }

    private boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {

        pointX -= this.x;
        pointY -= this.y;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

}