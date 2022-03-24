package fuzs.gamblingstyle.client.gui.screens.recipebook;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeSlot {
    public final int x, y;
    public int index;
    private boolean craftable;
    private Recipe<?> recipe;

    public RecipeSlot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRecipe(Recipe<?> recipe, boolean craftable) {
        this.recipe = recipe;
        this.craftable = craftable;
    }

    public Recipe<?> getRecipe() {
        return this.recipe;
    }

    public boolean hasRecipe() {
        return this.getRecipe() != null;
    }

    public ItemStack getItem() {
        return this.recipe.getResultItem();
    }
}
