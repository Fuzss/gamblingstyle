package com.fuzs.gamblingstyle.client.gui.core;

import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public interface ITooltipButton {

    void setPosition(int posX, int posY);

    List<String> getToolTip(GuiScreen screen, int mouseX, int mouseY);

}
