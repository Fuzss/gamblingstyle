package com.fuzs.gamblingstyle.client.gui.data;

import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.google.common.collect.Lists;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
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

    private boolean isSale() {

        return this.getItemToBuy().getItem() == Items.EMERALD || this.getSecondItemToBuy().getItem() == Items.EMERALD;
    }

    private boolean isPurchase() {

        return this.getItemToSell().getItem() == Items.EMERALD;
    }

    public boolean shouldBeIncluded(ITradingInfo.FilterMode mode) {

        if (mode.isBuys() && this.isPurchase()) {

            return true;
        } else if (mode.isSells() && this.isSale()) {

            return true;
        }

        return !this.isSale() && !this.isPurchase();
    }

    public boolean hasRecipeContents() {

        boolean secondItem = !this.hasSecondItemToBuy() || (this.secondItemIngredients >= this.getSecondItemToBuy().getCount());
        return secondItem && this.itemIngredients >= this.getItemToBuy().getCount();
    }

    public List<String> getSearchTooltip(ITooltipFlag tooltipFlag) {

        List<String> list = Lists.newArrayList();
        list.addAll(this.getItemToBuy().getTooltip(null, tooltipFlag));
        list.addAll(this.getItemToSell().getTooltip(null, tooltipFlag));
        if (this.hasSecondItemToBuy()) {

            list.addAll(this.getSecondItemToBuy().getTooltip(null, tooltipFlag));
        }

        return list;
    }

}
