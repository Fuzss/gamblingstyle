package fuzs.gamblingstyle.client.handler;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fuzs.gamblingstyle.GamblingStyle;
import fuzs.puzzleslib.json.JsonConfigFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.compress.utils.Sets;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RecipeFavoritesManager {
    public static final RecipeFavoritesManager INSTANCE = new RecipeFavoritesManager();

    private final Map<RecipeType<?>, Set<ResourceLocation>> typeToFavorites = Maps.newHashMap();

    public boolean toggleFavorite(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "Recipe may not be null");
        Set<ResourceLocation> recipes = this.typeToFavorites.computeIfAbsent(recipe.getType(), type -> Sets.newHashSet());
        boolean favorite;
        if (!recipes.contains(recipe.getId())) {
            recipes.add(recipe.getId());
            favorite = true;
        } else {
            recipes.remove(recipe.getId());
            favorite = false;
        }
        this.save();
        return favorite;
    }

    public boolean isFavorite(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "Recipe may not be null");
        Set<ResourceLocation> recipes = this.typeToFavorites.get(recipe.getType());
        if (recipes != null) {
            return recipes.contains(recipe.getId());
        }
        return false;
    }

    private void save() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<RecipeType<?>, Set<ResourceLocation>> entry : this.typeToFavorites.entrySet()) {
            JsonArray jsonElements = new JsonArray();
            for (ResourceLocation location : entry.getValue()) {
                jsonElements.add(location.toString());
            }
            jsonObject.add(Registry.RECIPE_TYPE.getKey(entry.getKey()).toString(), jsonElements);
        }
//        Minecraft.getInstance().hasSingleplayerServer()
//        File file = JsonConfigFileUtil.getSpecialConfigPath(, GamblingStyle.MOD_ID);
//        JsonConfigFileUtil.saveToFile(file, jsonObject);
    }

//    private File getFavoritesCache() {
//        if (Minecraft.getInstance().hasSingleplayerServer()) {
//
//        } else {
//            ServerData data = Minecraft.getInstance().getCurrentServer();
//            if (data != null) {
//                File file = JsonConfigFileUtil.getSpecialConfigPath(, String.format(".%scache", GamblingStyle.MOD_ID));
//            }
//        }
//    }

    public void load() {
        // TODO
    }
}
