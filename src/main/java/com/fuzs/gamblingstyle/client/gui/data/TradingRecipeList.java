package com.fuzs.gamblingstyle.client.gui.data;

import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TradingRecipeList extends ArrayList<TradingRecipe> {

    public TradingRecipeList(MerchantRecipeList list) {

        for (MerchantRecipe recipe : list) {

            if (this.isValidRecipe(recipe)) {

                this.add(new TradingRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell()));
            }
        }
    }

    private boolean isValidRecipe(MerchantRecipe recipe) {

        return !recipe.getItemToBuy().isEmpty() && !recipe.getItemToSell().isEmpty();
    }

    public int activeRecipeSize() {

        return Math.toIntExact(this.stream().filter(TradingRecipe::getActive).count());
    }

    /**
     * Searches trading recipes for a string, hides the ones not containing it
     *
     * @param query                    String to be searched for
     * @param advanced Get this setting from the game controller
     */
    public void search(String query, boolean advanced) {

        ITooltipFlag tooltipFlag = advanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        String trimmed = query.trim();
        int i = 0;
        if (!trimmed.isEmpty()) {
            if (trimmed.startsWith("\u003C")) { //less than
                trimmed = trimmed.substring(1);
                i = 1;
            } else if (trimmed.startsWith("\u003E")) { //greater than
                trimmed = trimmed.substring(1);
                i = 2;
            }
        }

        String s2 = trimmed.trim();
        for (TradingRecipe recipe : this) {
            if (!query.isEmpty()) {
                recipe.setActive(recipe.getCombinedTooltip(i, tooltipFlag).stream()
                        .map(it -> it.toLowerCase(Locale.ROOT)).anyMatch(it -> it.contains(s2)));
            } else {
                recipe.setActive(true);
            }
        }

    }

    /**
     * Scans the inventory each time it changes to see if it contains enough items to perform each trade
     *
     * @param container Current ContainerVillager
     */
    public void countRecipeContents(ContainerVillager container) {

        for (TradingRecipe recipe : this) {

            recipe.ingredients = 0;
            recipe.secondIngredients = 0;
        }

        List<ItemStack> collect = container.inventorySlots.stream().map(Slot::getStack).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {

            ItemStack itemstack = collect.get(i);
            if (i != 2) { //don't count output slot

                for (TradingRecipe recipe : this) {

                    if (ItemStack.areItemsEqual(itemstack, recipe.getItemToBuy())) {

                        recipe.ingredients += itemstack.getCount();
                    }

                    if (recipe.hasSecondItemToBuy() && ItemStack.areItemsEqual(itemstack, recipe.getSecondItemToBuy())) {

                        recipe.secondIngredients += itemstack.getCount();
                    }
                }
            }
        }
    }

}
