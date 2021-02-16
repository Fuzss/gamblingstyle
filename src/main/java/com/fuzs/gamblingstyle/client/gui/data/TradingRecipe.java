package com.fuzs.gamblingstyle.client.gui.data;

import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.google.common.collect.Lists;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
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
    private boolean favorite;
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

    public boolean isFavorite() {

        return this.favorite;
    }

    public void favorite() {

        this.favorite = true;
    }

    public void toggleFavorite() {

        this.favorite = !this.favorite;
    }

    private boolean isSale() {

        return this.getItemToBuy().getItem() == Items.EMERALD || this.getSecondItemToBuy().getItem() == Items.EMERALD;
    }

    private boolean isPurchase() {

        return this.getItemToSell().getItem() == Items.EMERALD;
    }

    public boolean shouldBeIncluded(ITradingInfo.FilterMode mode) {

        switch (mode) {

            case FAVORITES:

                return this.isFavorite();
            case SELLS:

                return this.isSale();
            case BUYS:

                return this.isPurchase();
        }

        return true;
    }

    public boolean hasRecipeContents() {

        boolean secondItem = !this.hasSecondItemToBuy() || (this.secondItemIngredients >= this.getSecondItemToBuy().getCount());
        return secondItem && this.itemIngredients >= this.getItemToBuy().getCount();
    }

    public List<String> getSearchTooltip(EntityPlayer player, ITooltipFlag tooltipFlag) {

        List<String> list = Lists.newArrayList();
        list.addAll(this.getItemToBuy().getTooltip(player, tooltipFlag));
        list.addAll(this.getItemToSell().getTooltip(player, tooltipFlag));
        if (this.hasSecondItemToBuy()) {

            list.addAll(this.getSecondItemToBuy().getTooltip(player, tooltipFlag));
        }

        return list;
    }

}
