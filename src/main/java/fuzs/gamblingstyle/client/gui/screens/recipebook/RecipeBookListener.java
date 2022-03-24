package fuzs.gamblingstyle.client.gui.screens.recipebook;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class RecipeBookListener implements ContainerListener {
    private final ModRecipeBookComponent recipeBook;

    public RecipeBookListener(ModRecipeBookComponent recipeBook) {
        this.recipeBook = recipeBook;
    }

    @Override
    public void slotChanged(AbstractContainerMenu p_39315_, int p_39316_, ItemStack p_39317_) {
        if (this.recipeBook.isVisible()) {
            this.recipeBook.updateStackedContents();
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu p_150524_, int p_150525_, int p_150526_) {

    }
}
