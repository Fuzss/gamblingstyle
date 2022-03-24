package fuzs.gamblingstyle.client.handler;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecipeBookMouseHandler {
    @SubscribeEvent
    public void onMouseReleased(final ScreenEvent.MouseReleasedEvent.Pre evt) {
        if (evt.getScreen() instanceof RecipeUpdateListener recipeUpdateListener) {
            RecipeBookComponent bookComponent = recipeUpdateListener.getRecipeBookComponent();
            if (bookComponent.mouseReleased(evt.getMouseX(), evt.getMouseY(), evt.getButton())) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMouseScroll(final ScreenEvent.MouseScrollEvent.Pre evt) {
        if (evt.getScreen() instanceof RecipeUpdateListener recipeUpdateListener) {
            RecipeBookComponent bookComponent = recipeUpdateListener.getRecipeBookComponent();
            if (bookComponent.mouseScrolled(evt.getMouseX(), evt.getMouseY(), evt.getScrollDelta())) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMouseDrag(final ScreenEvent.MouseDragEvent.Pre evt) {
        if (evt.getScreen() instanceof RecipeUpdateListener recipeUpdateListener) {
            RecipeBookComponent bookComponent = recipeUpdateListener.getRecipeBookComponent();
            if (bookComponent.mouseDragged(evt.getMouseX(), evt.getMouseY(), evt.getMouseButton(), evt.getDragX(), evt.getDragY())) {
                evt.setCanceled(true);
            }
        }
    }
}
