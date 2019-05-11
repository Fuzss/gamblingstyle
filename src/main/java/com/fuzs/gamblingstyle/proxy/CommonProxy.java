package com.fuzs.gamblingstyle.proxy;

import net.minecraft.entity.player.EntityPlayer;

public abstract class CommonProxy{

    public abstract EntityPlayer getClientPlayer();

    public abstract void showGuiScreen(Object clientGuiElement);

}