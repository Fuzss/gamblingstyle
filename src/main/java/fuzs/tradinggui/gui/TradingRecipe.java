package fuzs.tradinggui.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

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
    private boolean hasIngredients;
    private boolean isDisabled;

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

    public boolean isValidRecipe()
    {
        return !this.itemToBuy.isEmpty() && !this.itemToSell.isEmpty();
    }

    public List<String> getCombinedTooltip(Minecraft mc) {
        if (isValidRecipe()) {
            ITooltipFlag tooltipFlag = mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
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
