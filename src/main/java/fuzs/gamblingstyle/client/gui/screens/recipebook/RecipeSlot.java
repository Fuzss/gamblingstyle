package fuzs.gamblingstyle.client.gui.screens.recipebook;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class RecipeSlot {
    public final int x, y;
    public int index;
    @Nullable
    private Recipe<?> recipe;
    private boolean craftable;

    public RecipeSlot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRecipe(@Nullable Recipe<?> recipe, boolean craftable) {
        this.recipe = recipe;
        this.craftable = craftable;
    }

    @Nullable
    public Recipe<?> getRecipe() {
        return this.recipe;
    }

    public boolean hasRecipe() {
        return this.getRecipe() != null;
    }

    public ItemStack getItem() {
        return this.hasRecipe() ? this.recipe.getResultItem() : ItemStack.EMPTY;
    }

    public boolean hasCraftable() {
        return this.craftable;
    }
}