package fuzs.gamblingstyle.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy {

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void showGuiScreen(Object clientGuiElement) {
        GuiScreen gui = (GuiScreen) clientGuiElement;
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }
}
