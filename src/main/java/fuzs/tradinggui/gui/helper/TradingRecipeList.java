package fuzs.tradinggui.gui.helper;

import com.sun.istack.internal.NotNull;
import fuzs.tradinggui.inventory.ContainerVillager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class TradingRecipeList extends ArrayList<TradingRecipe> {

    public TradingRecipeList(@NotNull MerchantRecipeList list)
    {
        for (MerchantRecipe recipe : list) {
            this.add(new TradingRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell()));
        }
    }

    /**
     * Searches trading recipes for a string, hides the ones not containing it
     * @param s String to be searched for
     * @param advancedItemTooltips Get this setting from the game controller
     */
    public void searchQuery(String s, boolean advancedItemTooltips) {
        for (TradingRecipe recipe : this) {
            if (recipe.getCombinedTooltip(advancedItemTooltips) != null) {
                if (!s.isEmpty()) {
                    recipe.setIsSearchResult(recipe.getCombinedTooltip(advancedItemTooltips).stream()
                            .map(it -> it.toLowerCase(Locale.ROOT)).anyMatch(it -> it.contains(s)));
                } else {
                    recipe.setIsSearchResult(true);
                }
            }
        }

    }

    /**
     * Scans the inventory each time it changes to see if it contains enough items to perform each trade
     * @param container Current ContainerVillager
     */
    public void countRecipeContents(ContainerVillager container) {

        for (TradingRecipe recipe : this) {
            recipe.ingredients = 0;
            recipe.secoundIngredients = 0;
        }

        int i = 0;
        for (ItemStack itemstack : container.inventorySlots.stream().map(Slot::getStack).collect(Collectors.toList())) {
            if (i != 2) { //don't count output slot
                for (TradingRecipe recipe : this) {
                    if (ItemStack.areItemsEqual(itemstack, recipe.getItemToBuy())) {
                        recipe.ingredients += itemstack.getCount();
                    }
                    if (recipe.hasSecondItemToBuy() && ItemStack.areItemsEqual(itemstack, recipe.getSecondItemToBuy())) {
                        recipe.secoundIngredients += itemstack.getCount();
                    }
                }
            }
            i++;
        }
    }

}
