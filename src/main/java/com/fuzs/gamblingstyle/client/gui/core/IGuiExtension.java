package com.fuzs.gamblingstyle.client.gui.core;

import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import net.minecraft.client.Minecraft;
import net.minecraft.village.MerchantRecipeList;

public interface IGuiExtension {

    void initGui(Minecraft mc, int width, int height);

    void onGuiClosed();

    void updateScreen(MerchantRecipeList merchantrecipelist, ContainerVillager container);

    void drawScreen(int mouseX, int mouseY, float partialTicks);

    boolean mouseClicked(int mouseX, int mouseY, int mouseButton);

    void mouseReleased(int mouseX, int mouseY, int state);

    void handleMouseInput();

    boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize);

    boolean keyTyped(char typedChar, int keyCode);

}
