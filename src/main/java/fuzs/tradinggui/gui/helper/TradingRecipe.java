package fuzs.tradinggui.gui.helper;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import scala.Array;

import java.util.ArrayList;
import java.util.List;

public class TradingRecipe {

    /** Item the Villager buys. */
    private ItemStack itemToBuy;
    /** Second Item the Villager buys. */
    private ItemStack secondItemToBuy;
    /** Item the Villager sells. */
    private ItemStack itemToSell;

    private boolean isSearchResult;
    private boolean isSelected;
    public int ingredients;
    public int secoundIngredients;

    public TradingRecipe(ItemStack buy1, ItemStack buy2, ItemStack sell)
    {
        this.itemToBuy = ItemStack.EMPTY;
        this.secondItemToBuy = ItemStack.EMPTY;
        this.itemToSell = ItemStack.EMPTY;
        this.itemToBuy = buy1;
        this.secondItemToBuy = buy2;
        this.itemToSell = sell;
        this.isSearchResult = true;
        this.isSelected = false;
        this.ingredients = 0;
        this.secoundIngredients = 0;
    }

    /**
     * Gets the itemToBuy.
     */
    public ItemStack getItemToBuy()
    {
        return this.itemToBuy;
    }

    /**
     * Gets secondItemToBuy.
     */
    public ItemStack getSecondItemToBuy()
    {
        return this.secondItemToBuy;
    }

    /**
     * Gets if Villager has secondItemToBuy.
     */
    public boolean hasSecondItemToBuy()
    {
        return !this.secondItemToBuy.isEmpty();
    }

    /**
     * Gets itemToSell.
     */
    public ItemStack getItemToSell()
    {
        return this.itemToSell;
    }

    public boolean getIsSearchResult() {
        return isSearchResult;
    }

    public void setIsSearchResult(boolean flag) {
        this.isSearchResult = flag;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean flag) {
        this.isSelected = flag;
    }

    /**
     * Checks if a trading recipe has an input and output, used to determine if the recipe should be rendered
     */
    public boolean isValidRecipe()
    {
        return !this.itemToBuy.isEmpty() && !this.itemToSell.isEmpty();
    }

    /**
     * Returns if the player has enough items for a trade in their inventory
     */
    public boolean hasRecipeContents() {
        boolean flag = !this.hasSecondItemToBuy() || (this.secoundIngredients >= this.secondItemToBuy.getCount());
        return flag && this.ingredients >= this.itemToBuy.getCount();
    }

    /**
     * @param advancedItemTooltips Get this setting from the game controller
     * @return List containing all tooltips of the items involved with this trading recipe
     */
    public List<String> getCombinedTooltip(boolean advancedItemTooltips) {
        if (isValidRecipe()) {
            ITooltipFlag tooltipFlag = advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
            List<String> list = Lists.newArrayList();
            list.addAll(this.itemToBuy.getTooltip(null, tooltipFlag));
            list.addAll(this.itemToSell.getTooltip(null, tooltipFlag));
            if (this.hasSecondItemToBuy()) {
                list.addAll(this.secondItemToBuy.getTooltip(null, tooltipFlag));
            }
            return list;
        }
        return null;
    }
}
