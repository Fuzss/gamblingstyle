package com.fuzs.gamblingstyle.client.gui.data;

import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.google.common.collect.Lists;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

import java.util.List;

public class TradingRecipe {

    int ingredients;
    int secondIngredients;
    private ItemStack itemToBuy;
    private ItemStack secondItemToBuy;
    private ItemStack itemToSell;
    private boolean active;
    private boolean selected;

    public TradingRecipe(ItemStack buy1, ItemStack buy2, ItemStack sell) {

        this.itemToBuy = ItemStack.EMPTY;
        this.secondItemToBuy = ItemStack.EMPTY;
        this.itemToSell = ItemStack.EMPTY;
        this.itemToBuy = buy1;
        this.secondItemToBuy = buy2;
        this.itemToSell = sell;
        this.active = true;
        this.selected = false;
        this.ingredients = 0;
        this.secondIngredients = 0;
    }

    /**
     * Gets the itemToBuy.
     */
    public ItemStack getItemToBuy() {
        return this.itemToBuy;
    }

    /**
     * Gets secondItemToBuy.
     */
    public ItemStack getSecondItemToBuy() {
        return this.secondItemToBuy;
    }

    /**
     * Gets if Villager has secondItemToBuy.
     */
    public boolean hasSecondItemToBuy() {
        return !this.secondItemToBuy.isEmpty();
    }

    /**
     * Gets itemToSell.
     */
    public ItemStack getItemToSell() {
        return this.itemToSell;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean flag) {
        this.active = flag;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean flag) {
        this.selected = flag;
    }

    /**
     * Returns if the player has enough items for a trade in their inventory
     */
    public boolean hasRecipeContents() {

        boolean flag = !this.hasSecondItemToBuy() || (this.secondIngredients >= this.secondItemToBuy.getCount());
        return flag && this.ingredients >= this.itemToBuy.getCount();
    }

    /**
     * @param tooltipFlag Get this setting from the game controller
     * @return List containing all tooltips of the items involved with this trading recipe
     */
    public List<String> getCombinedTooltip(ITradingInfo.FilterMode mode, ITooltipFlag tooltipFlag) {

        List<String> list = Lists.newArrayList();
        if (mode != ITradingInfo.FilterMode.SELLS) {

            list.addAll(this.itemToBuy.getTooltip(null, tooltipFlag));
            if (this.hasSecondItemToBuy()) {

                list.addAll(this.secondItemToBuy.getTooltip(null, tooltipFlag));
            }
        }

        if (mode != ITradingInfo.FilterMode.BUYS) {

            list.addAll(this.itemToSell.getTooltip(null, tooltipFlag));
        }

        return list;

    }
}
