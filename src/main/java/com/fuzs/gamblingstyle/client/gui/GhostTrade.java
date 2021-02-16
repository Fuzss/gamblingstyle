package com.fuzs.gamblingstyle.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.stream.Stream;

@SideOnly(Side.CLIENT)
public class GhostTrade {
    
    private final int[][] slotCoordinates = {{76, 22}, {76, 48}, {134, 35}};
    private final ItemStack[] recipe = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private Minecraft mc;

    public void initGui(Minecraft mc) {
        
        this.mc = mc;
    }

    public void setRecipe(ItemStack itemToBuy, ItemStack secondItemToBuy, ItemStack itemToSell) {
        
        this.recipe[0] = itemToBuy;
        this.recipe[1] = secondItemToBuy;
        this.recipe[2] = itemToSell;
    }

    public void clear() {

        Arrays.fill(this.recipe, ItemStack.EMPTY);
    }

    private boolean isVisible() {

        return Stream.of(this.recipe).anyMatch(ingredient -> ingredient != ItemStack.EMPTY);
    }

    public void render(int left, int top) {
        
        if (this.isVisible()) {
            
            for (int i = 0; i < this.recipe.length; i++) {

                int[] slotCoordinates = this.slotCoordinates[i];
                this.drawGhostItem(this.recipe[i], slotCoordinates[0] + left, slotCoordinates[1] + top, i == 2);
            }
        }
    }

    private void drawGhostItem(ItemStack itemstack, int posX, int posY, boolean isOutputSlot) {

        if (itemstack.isEmpty()) {

            return;
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        if (isOutputSlot) {

            // draw this one larger than others
            Gui.drawRect(posX - 4, posY - 4, posX + 20, posY + 20, 822018048);
        } else {

            Gui.drawRect(posX, posY, posX + 16, posY + 16, 822018048);
        }

        GlStateManager.disableLighting();
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.mc.player, itemstack, posX, posY);
        GlStateManager.depthFunc(516);
        Gui.drawRect(posX, posY, posX + 16, posY + 16, 822083583);
        GlStateManager.depthFunc(515);
        this.mc.getRenderItem().renderItemOverlays(this.mc.fontRenderer, itemstack, posX, posY);
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderHoveredTooltip(int mouseX, int mouseY, int left, int top) {
        
        if (this.isVisible()) {
            
            ItemStack itemstack = this.findHoveredContents(mouseX, mouseY, left, top);
            if (!itemstack.isEmpty() && this.mc.currentScreen != null) {

                this.mc.currentScreen.drawHoveringText(this.mc.currentScreen.getItemToolTip(itemstack), mouseX, mouseY);
            }
        }
    }

    private ItemStack findHoveredContents(int mouseX, int mouseY, int left, int top) {

        ItemStack itemstack = ItemStack.EMPTY;
        for (int i = 0; i < this.recipe.length; i++) {

            int[] slotCoordinates = this.slotCoordinates[i];
            int posX = slotCoordinates[0] + left;
            int posY = slotCoordinates[1] + top;
            if (mouseX >= posX && mouseY >= posY && mouseX < posX + 16 && mouseY < posY + 16) {

                itemstack = this.recipe[i];
            }
        }

        return itemstack;
    }

}