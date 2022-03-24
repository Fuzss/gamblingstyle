package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.client.gui.screens.recipebook.ModRecipeBookComponent;
import fuzs.gamblingstyle.mixin.client.accessor.CraftingScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecipeBookExchangeHandler {
    @SubscribeEvent
    public void onScreenOpen(final ScreenOpenEvent evt) {
        if (evt.getScreen() instanceof CraftingScreen) {
            ((CraftingScreenAccessor) evt.getScreen()).setRecipeBookComponent(new ModRecipeBookComponent());
        }
    }
}
