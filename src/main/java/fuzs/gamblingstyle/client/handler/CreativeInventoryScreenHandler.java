package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.client.gui.screens.inventory.ModCreativeModeInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CreativeInventoryScreenHandler {
    @SubscribeEvent
    public void onScreenOpen(final ScreenOpenEvent evt) {
        if (evt.getScreen() instanceof CreativeModeInventoryScreen) {
            Minecraft minecraft = Minecraft.getInstance();
            evt.setScreen(new ModCreativeModeInventoryScreen(minecraft.player));
        }
    }
}
