package com.fuzs.gamblingstyle.client.handler;

import com.fuzs.gamblingstyle.client.gui.GuiVillager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OpenGuiHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent evt) {

        // vanilla gui will be opened after ours
        if (evt.getGui() instanceof GuiMerchant && Minecraft.getMinecraft().currentScreen instanceof GuiVillager) {

            evt.setCanceled(true);
        }
    }

}
