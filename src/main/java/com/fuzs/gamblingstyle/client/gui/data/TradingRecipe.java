package com.fuzs.gamblingstyle.client.gui.data;

import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.google.common.collect.Lists;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TradingRecipe {

    private final ItemStack[] recipe = new ItemStack[3];
    private boolean visible = true;
    private boolean selected;
    int itemIngredients;
    int secondItemIngredients;

    public TradingRecipe(ItemStack itemToBuy, ItemStack secondItemToBuy, ItemStack itemToSell) {

        this.recipe[0] = itemToBuy;
        this.recipe[1] = secondItemToBuy;
        this.recipe[2] = itemToSell;
    }

    public ItemStack getItemToBuy() {

        return this.recipe[0];
    }

    public ItemStack getSecondItemToBuy() {

        return this.recipe[1];
    }

    public boolean hasSecondItemToBuy() {

        return !this.getSecondItemToBuy().isEmpty();
    }

    public ItemStack getItemToSell() {

        return this.recipe[2];
    }

    public boolean isVisible() {

        return this.visible;
    }

    public void setVisible(boolean visible) {

        this.visible = visible;
    }

    public boolean isSelected() {

        return this.selected;
    }

    public void setSelected(boolean selected) {

        this.selected = selected;
    }

    /**
     * Returns if the player has enough items for a trade in their inventory
     */
    public boolean hasRecipeContents() {

        boolean secondItem = !this.hasSecondItemToBuy() || (this.secondItemIngredients >= this.getSecondItemToBuy().getCount());
        return secondItem && this.itemIngredients >= this.getItemToBuy().getCount();
    }

    public List<String> getCombinedTooltip(ITradingInfo.FilterMode mode, ITooltipFlag tooltipFlag) {

        List<String> list = Lists.newArrayList();
        if (mode != ITradingInfo.FilterMode.SELLS) {

            list.addAll(this.getItemToBuy().getTooltip(null, tooltipFlag));
            if (this.hasSecondItemToBuy()) {

                list.addAll(this.getSecondItemToBuy().getTooltip(null, tooltipFlag));
            }
        }

        if (mode != ITradingInfo.FilterMode.BUYS) {

            list.addAll(this.getItemToSell().getTooltip(null, tooltipFlag));
        }

        return list;
    }

}
