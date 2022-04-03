package fuzs.gamblingstyle.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.client.gui.widget.IconButton;
import fuzs.gamblingstyle.client.gui.widget.TabIconButton;
import fuzs.gamblingstyle.client.handler.CreativeSearchTreeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
@OnlyIn(Dist.CLIENT)
public class ModCreativeModeInventoryScreen extends CreativeModeInventoryScreen {
   private static final ResourceLocation CREATIVE_INVENTORY_LOCATION = new ResourceLocation(GamblingStyle.MOD_ID, "textures/gui/container/creative_inventory/creative_inventory.png");
   private static final int NUM_COLS = 6;
   private static final int NUM_ROWS = 7;

   private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
   private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   static final SimpleContainer CONTAINER = new SimpleContainer(NUM_COLS * NUM_ROWS);
   private static final Component TRASH_SLOT_TOOLTIP = new TranslatableComponent("inventory.binSlot");
   private static final int TEXT_COLOR = -1977417;
   private static int selectedTab = CreativeModeTab.TAB_SEARCH.getId();



   private float scrollOffs;
   private boolean scrolling;
   private EditBox searchBox;
   private IconButton leftPageButton;
   private IconButton rightPageButton;
   private final TabIconButton[] tabIconButtons = new TabIconButton[11];
   private TabIconButton selectedTabButton;




   @Nullable
   private List<Slot> originalSlots;
   @Nullable
   private Slot destroyItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTextInput;
   private static int tabPage = 0;
   private int maxPages = 0;
   private boolean hasClickedOutside;
   private final Set<TagKey<Item>> visibleTags = new HashSet<>();


   private int mouseX;
   private int mouseY;

   public ModCreativeModeInventoryScreen(Player p_98519_) {
      super(p_98519_);
      p_98519_.containerMenu = this.menu;
      this.passEvents = true;
      this.imageHeight = 166;
      this.imageWidth = 324;
   }

   @Override
   public void containerTick() {
      super.containerTick();
      if (!this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
      } else if (this.searchBox != null) {
         this.searchBox.tick();
      }
   }

   @Override
   protected void slotClicked(@Nullable Slot p_98556_, int p_98557_, int p_98558_, ClickType p_98559_) {
      if (this.isCreativeSlot(p_98556_)) {
         this.searchBox.moveCursorToEnd();
         this.searchBox.setHighlightPos(0);
      }

      boolean flag = p_98559_ == ClickType.QUICK_MOVE;
      p_98559_ = p_98557_ == -999 && p_98559_ == ClickType.PICKUP ? ClickType.THROW : p_98559_;
      if (p_98556_ == null && selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && p_98559_ != ClickType.QUICK_CRAFT) {
         if (!this.menu.getCarried().isEmpty() && this.hasClickedOutside) {
            if (p_98558_ == 0) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            }

            if (p_98558_ == 1) {
               ItemStack itemstack5 = this.menu.getCarried().split(1);
               this.minecraft.player.drop(itemstack5, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
            }
         }
      } else {
         if (p_98556_ != null && !p_98556_.mayPickup(this.minecraft.player)) {
            return;
         }

         if (p_98556_ == this.destroyItemSlot && flag) {
            for(int j = 0; j < this.minecraft.player.inventoryMenu.getItems().size(); ++j) {
               this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, j);
            }
         } else if (selectedTab == CreativeModeTab.TAB_INVENTORY.getId()) {
            if (p_98556_ == this.destroyItemSlot) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else if (p_98559_ == ClickType.THROW && p_98556_ != null && p_98556_.hasItem()) {
               ItemStack itemstack = p_98556_.remove(p_98558_ == 0 ? 1 : p_98556_.getItem().getMaxStackSize());
               ItemStack itemstack1 = p_98556_.getItem();
               this.minecraft.player.drop(itemstack, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack);
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, ((ModCreativeModeInventoryScreen.SlotWrapper)p_98556_).target.index);
            } else if (p_98559_ == ClickType.THROW && !this.menu.getCarried().isEmpty()) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            } else {
               this.minecraft.player.inventoryMenu.clicked(p_98556_ == null ? p_98557_ : ((ModCreativeModeInventoryScreen.SlotWrapper)p_98556_).target.index, p_98558_, p_98559_, this.minecraft.player);
               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         } else if (p_98559_ != ClickType.QUICK_CRAFT && p_98556_.container == CONTAINER) {
            ItemStack itemstack4 = this.menu.getCarried();
            ItemStack itemstack7 = p_98556_.getItem();
            if (p_98559_ == ClickType.SWAP) {
               if (!itemstack7.isEmpty()) {
                  ItemStack itemstack10 = itemstack7.copy();
                  itemstack10.setCount(itemstack10.getMaxStackSize());
                  this.minecraft.player.getInventory().setItem(p_98558_, itemstack10);
                  this.minecraft.player.inventoryMenu.broadcastChanges();
               }

               return;
            }

            if (p_98559_ == ClickType.CLONE) {
               if (this.menu.getCarried().isEmpty() && p_98556_.hasItem()) {
                  ItemStack itemstack9 = p_98556_.getItem().copy();
                  itemstack9.setCount(itemstack9.getMaxStackSize());
                  this.menu.setCarried(itemstack9);
               }

               return;
            }

            if (p_98559_ == ClickType.THROW) {
               if (!itemstack7.isEmpty()) {
                  ItemStack itemstack8 = itemstack7.copy();
                  itemstack8.setCount(p_98558_ == 0 ? 1 : itemstack8.getMaxStackSize());
                  this.minecraft.player.drop(itemstack8, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack8);
               }

               return;
            }

            if (!itemstack4.isEmpty() && !itemstack7.isEmpty() && itemstack4.sameItem(itemstack7) && ItemStack.tagMatches(itemstack4, itemstack7)) {
               if (p_98558_ == 0) {
                  if (flag) {
                     itemstack4.setCount(itemstack4.getMaxStackSize());
                  } else if (itemstack4.getCount() < itemstack4.getMaxStackSize()) {
                     itemstack4.grow(1);
                  }
               } else {
                  itemstack4.shrink(1);
               }
            } else if (!itemstack7.isEmpty() && itemstack4.isEmpty()) {
               this.menu.setCarried(itemstack7.copy());
               itemstack4 = this.menu.getCarried();
               if (flag) {
                  itemstack4.setCount(itemstack4.getMaxStackSize());
               }
            } else if (p_98558_ == 0) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else {
               this.menu.getCarried().shrink(1);
            }
         } else if (this.menu != null) {
            ItemStack itemstack3 = p_98556_ == null ? ItemStack.EMPTY : this.menu.getSlot(p_98556_.index).getItem();
            this.menu.clicked(p_98556_ == null ? p_98557_ : p_98556_.index, p_98558_, p_98559_, this.minecraft.player);
            if (AbstractContainerMenu.getQuickcraftHeader(p_98558_) == 2) {
               for(int k = 0; k < 9; ++k) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + k).getItem(), 36 + k);
               }
            } else if (p_98556_ != null) {
               ItemStack itemstack6 = this.menu.getSlot(p_98556_.index).getItem();
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, p_98556_.index - this.menu.slots.size() + 9 + 36);
               int i = 45 + p_98558_;
               if (p_98559_ == ClickType.SWAP) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack3, i - this.menu.slots.size() + 9 + 36);
               } else if (p_98559_ == ClickType.THROW && !itemstack3.isEmpty()) {
                  ItemStack itemstack2 = itemstack3.copy();
                  itemstack2.setCount(p_98558_ == 0 ? 1 : itemstack2.getMaxStackSize());
                  this.minecraft.player.drop(itemstack2, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack2);
               }

               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         }
      }

   }

   private boolean isCreativeSlot(@Nullable Slot slot) {
      return slot != null && slot.container == CONTAINER;
   }

   @Override
   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         super.init();
         this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
         this.initTabIconButtons();
         this.initTabPageButtons();
         this.initSearchBox();
         int lastSelectedTab = selectedTab;
         selectedTab = -1;
         this.selectTab(CreativeModeTab.TABS[lastSelectedTab]);
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
         this.listener = new CreativeInventoryListener(this.minecraft);
         this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
      } else {
         this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
      }
   }

   private void initTabIconButtons() {
      for (int i = 0; i < this.tabIconButtons.length; i++) {
         this.tabIconButtons[i] = new TabIconButton(this.leftPos + i % 6 * 25, this.topPos - 24 + i / 6 * 192, 22, 22, 324, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
            if (this.selectedTabButton != null) {
               this.selectedTabButton.markCreativeTab(false);
            }
            this.selectedTabButton = (TabIconButton) button;
            this.selectedTabButton.markCreativeTab(true);
            this.selectTab(this.selectedTabButton.getCreativeTab());
         }, (Button button, PoseStack poseStack, int mouseX, int mouseY) -> {
            this.renderTooltip(poseStack, ((TabIconButton) button).getCreativeTab().getDisplayName(), mouseX, mouseY);
         });
      }
      for (TabIconButton tabIconButton : this.tabIconButtons) {
         this.addRenderableWidget(tabIconButton);
      }
   }

   private void initTabPageButtons() {
      this.maxPages = (int) Math.ceil((CreativeModeTab.TABS.length - 12) / 10.0);
      this.leftPageButton = new IconButton(this.leftPos + 125, this.topPos + 169, 11, 20, 368, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
         tabPage = Math.max(tabPage - 1, 0);
         this.updateTabIconButtons();
         this.updatePageButtons();
      });
      this.rightPageButton = new IconButton(this.leftPos + 136, this.topPos + 169, 11, 20, 379, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
         tabPage = Math.min(tabPage + 1, this.maxPages);
         this.updateTabIconButtons();
         this.updatePageButtons();
      });
      this.addRenderableWidget(this.leftPageButton);
      this.addRenderableWidget(this.rightPageButton);
      this.updatePageButtons();
   }

   private void initSearchBox() {
      this.searchBox = new EditBox(this.font, this.leftPos + 28, this.topPos + 14, 80, 9, new TranslatableComponent("itemGroup.search")) {
         @Override
         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // left click clears text
            if (this.isVisible() && button == 1 && !this.getValue().isEmpty()) {
               this.setValue("");
               ModCreativeModeInventoryScreen.this.refreshSearchResults();
            }
            return super.mouseClicked(mouseX, mouseY, button);
         }
      };
      this.searchBox.setMaxLength(50);
      this.searchBox.setBordered(false);
      this.searchBox.setTextColor(TEXT_COLOR);
      this.searchBox.setCanLoseFocus(false);
      this.searchBox.setFocus(true);
      this.addWidget(this.searchBox);
   }

   private void updateTabIconButtons() {
      int start = tabPage == 0 ? 0 : 2 + tabPage * 10;
      for (int i = 0, j = 0; i < this.tabIconButtons.length; i++) {
         if (i == 6) {
            this.tabIconButtons[i].setCreativeTab(CreativeModeTab.TAB_SEARCH);
         } else {
            int tabIndex = start + j++;
            CreativeModeTab tab = tabIndex < CreativeModeTab.TABS.length ? CreativeModeTab.TABS[tabIndex] : null;
            if (tabPage == 0) {
               // skip last tab before search on vanilla tabs screen since vanilla only has 9 tabs with content and to make search stand out
               if (i == 5) {
                  tab = null;
                  j--;
               }
               // skip tab ids for saved hotbars tab (which we don't support) and search (which is hardcoded)
               if (j == 4 || j == 5) {
                  j = 6;
               }
            }
            this.tabIconButtons[i].setCreativeTab(tab);
         }
      }
   }

   private void updatePageButtons() {
      if (CreativeModeTab.TABS.length > 12) {
         this.leftPageButton.active = tabPage != 0;
         this.rightPageButton.active = tabPage != this.maxPages;
      } else {
         this.leftPageButton.visible = false;
         this.rightPageButton.visible = false;
      }
   }

   @Override
   public void resize(Minecraft p_98595_, int p_98596_, int p_98597_) {
      String s = this.searchBox.getValue();
      this.init(p_98595_, p_98596_, p_98597_);
      this.searchBox.setValue(s);
      if (!this.searchBox.getValue().isEmpty()) {
         this.refreshSearchResults();
      }
   }

   @Override
   public void removed() {
      super.removed();
      if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
      }
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   @Override
   public boolean charTyped(char p_98521_, int p_98522_) {
      if (this.ignoreTextInput) {
         return false;
      } else {
         String s = this.searchBox.getValue();
         if (this.searchBox.charTyped(p_98521_, p_98522_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
               this.refreshSearchResults();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean keyPressed(int p_98547_, int p_98548_, int p_98549_) {
      this.ignoreTextInput = false;
      boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
      boolean flag1 = InputConstants.getKey(p_98547_, p_98548_).getNumericKeyValue().isPresent();
      if (flag && flag1 && this.checkHotbarKeyPressed(p_98547_, p_98548_)) {
         this.ignoreTextInput = true;
         return true;
      } else {
         String s = this.searchBox.getValue();
         if (this.searchBox.keyPressed(p_98547_, p_98548_, p_98549_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
               this.refreshSearchResults();
            }
            return true;
         } else {
            return this.searchBox.isFocused() && this.searchBox.isVisible() && p_98547_ != 256 || super.keyPressed(p_98547_, p_98548_, p_98549_);
         }
      }
   }

   @Override
   public boolean keyReleased(int p_98612_, int p_98613_, int p_98614_) {
      this.ignoreTextInput = false;
      return super.keyReleased(p_98612_, p_98613_, p_98614_);
   }

   private void refreshSearchResults() {
      this.menu.items.clear();
      this.visibleTags.clear();
      CreativeModeTab tab = CreativeModeTab.TABS[selectedTab];
      String s = this.searchBox.getValue();
      if (s.isEmpty()) {
         for (Item item : ForgeRegistries.ITEMS) {
            item.fillItemCategory(tab, this.menu.items);
         }
      } else {
         TooltipFlag.Default tooltipFlag = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
         SearchTree<ItemStack> searchtree;
         if (s.startsWith("#")) {
            s = s.substring(1);
            searchtree = CreativeSearchTreeManager.INSTANCE.getTabTagsSearchTree(tab, tooltipFlag);
            this.updateVisibleTags(s);
         } else {
            searchtree = CreativeSearchTreeManager.INSTANCE.getTabNamesSearchTree(tab, tooltipFlag);
         }
         this.menu.items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
      }
      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   private void updateVisibleTags(String p_98620_) {
      int i = p_98620_.indexOf(58);
      Predicate<ResourceLocation> predicate;
      if (i == -1) {
         predicate = p_98609_ -> {
            return p_98609_.getPath().contains(p_98620_);
         };
      } else {
         String s = p_98620_.substring(0, i).trim();
         String s1 = p_98620_.substring(i + 1).trim();
         predicate = p_98606_ -> {
            return p_98606_.getNamespace().contains(s) && p_98606_.getPath().contains(s1);
         };
      }

      Registry.ITEM.getTagNames().filter(p_205410_ -> {
         return predicate.test(p_205410_.location());
      }).forEach(this.visibleTags::add);
   }

   @Override
   protected void renderLabels(PoseStack p_98616_, int p_98617_, int p_98618_) {

   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (this.insideScrollbar(this.mouseX, this.mouseY)) {
            if (this.canScroll()) {
               this.scrolling = true;
               this.mouseDragged(this.mouseX, this.mouseY, button, 0.0, 0.0);
            }
            return true;
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseReleased(double p_98622_, double p_98623_, int p_98624_) {
      if (p_98624_ == 0) {
         this.scrolling = false;
      }
      return super.mouseReleased(p_98622_, p_98623_, p_98624_);
   }

   private boolean canScroll() {
      if (CreativeModeTab.TABS[selectedTab] == null) return false;
      return this.menu.canScroll();
   }

   private void selectTab(CreativeModeTab tab) {
      if (tab == null) return;
      int i = selectedTab;
      selectedTab = tab.getId();
      this.slotColor = tab.getSlotColor();
      this.quickCraftSlots.clear();
      this.menu.items.clear();
      if (tab == CreativeModeTab.TAB_HOTBAR) {
         HotbarManager hotbarmanager = this.minecraft.getHotbarManager();

         for(int j = 0; j < 9; ++j) {
            Hotbar hotbar = hotbarmanager.get(j);
            if (hotbar.isEmpty()) {
               for(int k = 0; k < 9; ++k) {
                  if (k == j) {
                     ItemStack itemstack = new ItemStack(Items.PAPER);
                     itemstack.getOrCreateTagElement("CustomCreativeLock");
                     Component component = this.minecraft.options.keyHotbarSlots[j].getTranslatedKeyMessage();
                     Component component1 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                     itemstack.setHoverName(new TranslatableComponent("inventory.hotbarInfo", component1, component));
                     this.menu.items.add(itemstack);
                  } else {
                     this.menu.items.add(ItemStack.EMPTY);
                  }
               }
            } else {
               this.menu.items.addAll(hotbar);
            }
         }
      } else if (tab != CreativeModeTab.TAB_SEARCH) {
         tab.fillItemList(this.menu.items);
      }

      if (tab == CreativeModeTab.TAB_INVENTORY) {
         AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
         if (this.originalSlots == null) {
            this.originalSlots = ImmutableList.copyOf(this.menu.slots);
         }

         this.menu.slots.clear();

         for(int l = 0; l < abstractcontainermenu.slots.size(); ++l) {
            int i1;
            int j1;
            if (l >= 5 && l < 9) {
               int l1 = l - 5;
               int j2 = l1 / 2;
               int l2 = l1 % 2;
               i1 = 54 + j2 * 54;
               j1 = 6 + l2 * 27;
            } else if (l >= 0 && l < 5) {
               i1 = -2000;
               j1 = -2000;
            } else if (l == 45) {
               i1 = 35;
               j1 = 20;
            } else {
               int k1 = l - 9;
               int i2 = k1 % 9;
               int k2 = k1 / 9;
               i1 = 9 + i2 * 18;
               if (l >= 36) {
                  j1 = 112;
               } else {
                  j1 = 54 + k2 * 18;
               }
            }

            Slot slot = new ModCreativeModeInventoryScreen.SlotWrapper(abstractcontainermenu.slots.get(l), l, i1, j1);
            this.menu.slots.add(slot);
         }

         this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
         this.menu.slots.add(this.destroyItemSlot);
      } else if (i == CreativeModeTab.TAB_INVENTORY.getId()) {
         this.menu.slots.clear();
         this.menu.slots.addAll(this.originalSlots);
         this.originalSlots = null;
      }
      if (i != tab.getId()) {
         this.searchBox.setValue("");
      }
      this.refreshSearchResults();
      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   @Override
   public boolean mouseScrolled(double p_98527_, double p_98528_, double p_98529_) {
      if (!this.canScroll()) {
         return false;
      } else {
         int i = (this.menu.items.size() + NUM_COLS - 1) / NUM_COLS - NUM_ROWS;
         float f = (float) (p_98529_ / (double) i);
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      }
   }

   @Override
   protected boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int mouseButton) {
      boolean hasClickedOutside = mouseX < (double)leftPos || mouseY < (double)topPos || mouseX >= (double)(leftPos + this.imageWidth) || mouseY >= (double)(topPos + this.imageHeight);
      for (TabIconButton tabIconButton : this.tabIconButtons) {
         hasClickedOutside |= tabIconButton.isHoveredOrFocused();
      }
      hasClickedOutside |= this.leftPageButton.isHoveredOrFocused();
      hasClickedOutside |= this.rightPageButton.isHoveredOrFocused();
      this.hasClickedOutside = hasClickedOutside;
      return this.hasClickedOutside;
   }

   @Override
   protected boolean insideScrollbar(double mouseX, double mouseY) {
      int fromX = this.leftPos + 122;
      int fromY = this.topPos + 29;
      int toX = fromX + 14;
      int toY = fromY + 126;
      return mouseX >= (double)fromX && mouseY >= (double)fromY && mouseX < (double)toX && mouseY < (double)toY;
   }

   @Override
   public boolean mouseDragged(double p_98535_, double p_98536_, int p_98537_, double p_98538_, double p_98539_) {
      if (this.scrolling) {
         int i = this.topPos + 18;
         int j = i + 112;
         this.scrollOffs = ((float)p_98536_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      } else {
         return super.mouseDragged(p_98535_, p_98536_, p_98537_, p_98538_, p_98539_);
      }
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(poseStack);
      super.render(poseStack, mouseX, mouseY, partialTicks);
      if (this.destroyItemSlot != null && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, mouseX, mouseY)) {
         this.renderTooltip(poseStack, TRASH_SLOT_TOOLTIP, mouseX, mouseY);
      }
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.renderTooltip(poseStack, mouseX, mouseY);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   @Override
   protected void renderTooltip(PoseStack p_98590_, ItemStack p_98591_, int p_98592_, int p_98593_) {
      if (selectedTab == CreativeModeTab.TAB_SEARCH.getId()) {
         List<Component> list = p_98591_.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
         List<Component> list1 = Lists.newArrayList(list);
         Item item = p_98591_.getItem();
         CreativeModeTab creativemodetab = item.getItemCategory();
         if (creativemodetab == null && p_98591_.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(p_98591_);
            if (map.size() == 1) {
               Enchantment enchantment = map.keySet().iterator().next();

               for(CreativeModeTab creativemodetab1 : CreativeModeTab.TABS) {
                  if (creativemodetab1.hasEnchantmentCategory(enchantment.category)) {
                     creativemodetab = creativemodetab1;
                     break;
                  }
               }
            }
         }

         this.visibleTags.forEach(p_205407_ -> {
            if (p_98591_.is(p_205407_)) {
               list1.add(1, new TextComponent("#" + p_205407_.location()).withStyle(ChatFormatting.DARK_PURPLE));
            }

         });
         if (creativemodetab != null) {
            list1.add(1, creativemodetab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
         }

         this.renderTooltip(p_98590_, list1, p_98591_.getTooltipImage(), p_98592_, p_98593_, p_98591_);
      } else {
         super.renderTooltip(p_98590_, p_98591_, p_98592_, p_98593_);
      }

   }

   @Override
   protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, CREATIVE_INVENTORY_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      // book background
      blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 256);
      // search bar
      blit(poseStack, this.leftPos + 11, this.topPos + 10, 324, 126, 101, 16, 512, 256);
      // item slots
      blit(poseStack, this.leftPos + 11, this.topPos + 28, 324, 0, 109, 126, 512, 256);
      // scroll bar
      blit(poseStack, this.leftPos + 121, this.topPos + 28, 432, 0, 14, 126, 512, 256);
      // scrolling indicator
      int scrollX = this.leftPos + 122;
      int scrollYStart = this.topPos + 29;
      int scrollYEnd = scrollYStart + 126;
      blit(poseStack, scrollX, scrollYStart + (int) ((float) (scrollYEnd - scrollYStart - 17) * this.scrollOffs), 446 + (this.canScroll() ? 0 : 12), 0, 12, 15, 512, 256);
      this.searchBox.render(poseStack, mouseX, mouseY, partialTicks);
      InventoryScreen.renderEntityInInventory(this.leftPos + 89 + 148, this.topPos + 75, 30, this.leftPos + 89 + 148 - this.mouseX, this.topPos + 75 - 50 - this.mouseY, this.minecraft.player);
   }

   @Override
   protected boolean checkTabClicked(CreativeModeTab p_98563_, double p_98564_, double p_98565_) {
      return false;
   }

   @Override
   protected boolean checkTabHovering(PoseStack p_98585_, CreativeModeTab p_98586_, int p_98587_, int p_98588_) {
      return false;
   }

   @Override
   protected void renderTabButton(PoseStack p_98582_, CreativeModeTab p_98583_) {

   }

   @Override
   public int getSelectedTab() {
      return selectedTab;
   }

   @OnlyIn(Dist.CLIENT)
   static class CustomCreativeSlot extends Slot {
      public CustomCreativeSlot(Container p_98633_, int p_98634_, int p_98635_, int p_98636_) {
         super(p_98633_, p_98634_, p_98635_, p_98636_);
      }

      @Override
      public boolean mayPickup(Player p_98638_) {
         if (super.mayPickup(p_98638_) && this.hasItem()) {
            return this.getItem().getTagElement("CustomCreativeLock") == null;
         } else {
            return !this.hasItem();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ItemPickerMenu extends CreativeModeInventoryScreen.ItemPickerMenu {
      public final NonNullList<ItemStack> items = NonNullList.create();
      private final AbstractContainerMenu inventoryMenu;

      public ItemPickerMenu(Player p_98641_) {
         super(p_98641_);
         this.inventoryMenu = p_98641_.inventoryMenu;
         Inventory inventory = p_98641_.getInventory();

         for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new ModCreativeModeInventoryScreen.CustomCreativeSlot(ModCreativeModeInventoryScreen.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
            }
         }

         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
         }

         this.scrollTo(0.0F);
      }

      @Override
      public boolean stillValid(Player p_98645_) {
         return true;
      }

      @Override
      public void scrollTo(float p_98643_) {
         int i = (this.items.size() + 9 - 1) / 9 - 5;
         int j = (int)((double)(p_98643_ * (float)i) + 0.5D);
         if (j < 0) {
            j = 0;
         }

         for(int k = 0; k < 5; ++k) {
            for(int l = 0; l < 9; ++l) {
               int i1 = l + (k + j) * 9;
               if (i1 >= 0 && i1 < this.items.size()) {
                  ModCreativeModeInventoryScreen.CONTAINER.setItem(l + k * 9, this.items.get(i1));
               } else {
                  ModCreativeModeInventoryScreen.CONTAINER.setItem(l + k * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      @Override
      public boolean canScroll() {
         return this.items.size() > 42;
      }

      @Override
      public ItemStack quickMoveStack(Player p_98650_, int p_98651_) {
         if (p_98651_ >= this.slots.size() - 9 && p_98651_ < this.slots.size()) {
            Slot slot = this.slots.get(p_98651_);
            if (slot != null && slot.hasItem()) {
               slot.set(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      @Override
      public boolean canTakeItemForPickAll(ItemStack p_98647_, Slot p_98648_) {
         return p_98648_.container != ModCreativeModeInventoryScreen.CONTAINER;
      }

      @Override
      public boolean canDragTo(Slot p_98653_) {
         return p_98653_.container != ModCreativeModeInventoryScreen.CONTAINER;
      }

      @Override
      public ItemStack getCarried() {
         return this.inventoryMenu.getCarried();
      }

      @Override
      public void setCarried(ItemStack p_169751_) {
         this.inventoryMenu.setCarried(p_169751_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class SlotWrapper extends Slot {
      final Slot target;

      public SlotWrapper(Slot p_98657_, int p_98658_, int p_98659_, int p_98660_) {
         super(p_98657_.container, p_98658_, p_98659_, p_98660_);
         this.target = p_98657_;
      }

      @Override
      public void onTake(Player p_169754_, ItemStack p_169755_) {
         this.target.onTake(p_169754_, p_169755_);
      }

      @Override
      public boolean mayPlace(ItemStack p_98670_) {
         return this.target.mayPlace(p_98670_);
      }

      @Override
      public ItemStack getItem() {
         return this.target.getItem();
      }

      @Override
      public boolean hasItem() {
         return this.target.hasItem();
      }

      @Override
      public void set(ItemStack p_98679_) {
         this.target.set(p_98679_);
      }

      @Override
      public void setChanged() {
         this.target.setChanged();
      }

      @Override
      public int getMaxStackSize() {
         return this.target.getMaxStackSize();
      }

      @Override
      public int getMaxStackSize(ItemStack p_98675_) {
         return this.target.getMaxStackSize(p_98675_);
      }

      @Override
      @Nullable
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
         return this.target.getNoItemIcon();
      }

      @Override
      public ItemStack remove(int p_98663_) {
         return this.target.remove(p_98663_);
      }

      @Override
      public boolean isActive() {
         return this.target.isActive();
      }

      @Override
      public boolean mayPickup(Player p_98665_) {
         return this.target.mayPickup(p_98665_);
      }

      @Override
      public int getSlotIndex() {
         return this.target.getSlotIndex();
      }

      @Override
      public boolean isSameInventory(Slot other) {
         return this.target.isSameInventory(other);
      }

      @Override
      public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
         this.target.setBackground(atlas, sprite);
         return this;
      }
   }
}
