package fuzs.gamblingstyle.client.handler;

import fuzs.gamblingstyle.GamblingStyle;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public class CreativeSearchTreeManager {
    public static final CreativeSearchTreeManager INSTANCE = new CreativeSearchTreeManager();

    private final Minecraft minecraft = Minecraft.getInstance();
    private final Int2ObjectMap<Pair<SearchRegistry.Key<ItemStack>, SearchRegistry.Key<ItemStack>>> normalTabSearchTrees = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Pair<SearchRegistry.Key<ItemStack>, SearchRegistry.Key<ItemStack>>> advancedTabSearchTrees = new Int2ObjectOpenHashMap<>();

    public void load() {
        for (CreativeModeTab tab : CreativeModeTab.TABS) {
            if (tab != null && tab != CreativeModeTab.TAB_HOTBAR && tab != CreativeModeTab.TAB_INVENTORY) {
                GamblingStyle.LOGGER.info("Creating creative inventory search trees for tab {}", tab.getDisplayName().getString());
                this.normalTabSearchTrees.put(tab.getId(), this.buildTabSearchTrees(tab, TooltipFlag.Default.NORMAL));
                this.advancedTabSearchTrees.put(tab.getId(), this.buildTabSearchTrees(tab, TooltipFlag.Default.ADVANCED));
            }
        }
    }

    public MutableSearchTree<ItemStack> getTabNamesSearchTree(CreativeModeTab tab, TooltipFlag.Default tooltipFlag) {
        return this.getTabSearchTree(tab, tooltipFlag, false);
    }

    public MutableSearchTree<ItemStack> getTabTagsSearchTree(CreativeModeTab tab, TooltipFlag.Default tooltipFlag) {
        return this.getTabSearchTree(tab, tooltipFlag, true);
    }

    private MutableSearchTree<ItemStack> getTabSearchTree(CreativeModeTab tab, TooltipFlag.Default tooltipFlag, boolean tags) {
        Pair<SearchRegistry.Key<ItemStack>, SearchRegistry.Key<ItemStack>> tabSearchTrees;
        if (tooltipFlag.isAdvanced()) {
            tabSearchTrees = this.advancedTabSearchTrees.get(tab.getId());
        } else {
            tabSearchTrees = this.normalTabSearchTrees.get(tab.getId());
        }
        return this.minecraft.getSearchTree(tags ? tabSearchTrees.right() : tabSearchTrees.left());
    }

    private Pair<SearchRegistry.Key<ItemStack>, SearchRegistry.Key<ItemStack>> buildTabSearchTrees(CreativeModeTab tab, TooltipFlag.Default tooltipFlag) {
        ReloadableSearchTree<ItemStack> tabNamesSearchTree = new ReloadableSearchTree<>((p_210797_) -> {
            return p_210797_.getTooltipLines(null, tooltipFlag).stream().map((p_210807_) -> {
                return ChatFormatting.stripFormatting(p_210807_.getString()).trim();
            }).filter((p_210809_) -> {
                return !p_210809_.isEmpty();
            });
        }, (p_91317_) -> {
            return Stream.of(ForgeRegistries.ITEMS.getKey(p_91317_.getItem()));
        });
        ReloadableIdSearchTree<ItemStack> tabTagsSearchTree = new ReloadableIdSearchTree<>((p_91121_) -> {
            return p_91121_.getTags().map(TagKey::location);
        });
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        for(Item item : ForgeRegistries.ITEMS) {
            item.fillItemCategory(tab, nonnulllist);
        }
        nonnulllist.forEach((p_210707_) -> {
            tabNamesSearchTree.add(p_210707_);
            tabTagsSearchTree.add(p_210707_);
        });
        SearchRegistry.Key<ItemStack> tabNamesSearchTreeKey = this.registerSearchTree(tabNamesSearchTree);
        SearchRegistry.Key<ItemStack> tabTagsSearchTreeKey = this.registerSearchTree(tabTagsSearchTree);
        return Pair.of(tabNamesSearchTreeKey, tabTagsSearchTreeKey);
    }

    private SearchRegistry.Key<ItemStack> registerSearchTree(MutableSearchTree<ItemStack> searchTree) {
        SearchRegistry.Key<ItemStack> searchTreeKey = new SearchRegistry.Key<>();
        this.minecraft.getSearchTreeManager().register(searchTreeKey, searchTree);
        return searchTreeKey;
    }
}
