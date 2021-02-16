package com.fuzs.gamblingstyle.client.gui;

import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public interface ITooltipButton {

    List<String> getToolTip(GuiScreen screen, int mouseX, int mouseY);

}
