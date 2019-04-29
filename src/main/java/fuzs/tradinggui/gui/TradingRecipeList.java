package fuzs.tradinggui.gui;

import com.sun.istack.internal.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.Locale;

public class TradingRecipeList extends ArrayList<TradingRecipe> {

    public TradingRecipeList(@NotNull MerchantRecipeList list)
    {
        for (MerchantRecipe recipe : list) {
            this.add(new TradingRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell()));
        }
    }

    public void searchQuery(String s, Minecraft mc) {
        for (TradingRecipe recipe : this) {
            if (recipe.getCombinedTooltip(mc) != null) {
                if (!s.isEmpty()) {
                    recipe.setIsSearchResult(recipe.getCombinedTooltip(mc).stream()
                            .map(it -> it.toLowerCase(Locale.ROOT)).anyMatch(it -> it.contains(s)));
                } else {
                    recipe.setIsSearchResult(true);
                }
            }
        }

    }

}
