package fuzs.gamblingstyle.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.client.gui.widget.IconButton;
import fuzs.gamblingstyle.client.gui.widget.TabIconButton;
import fuzs.gamblingstyle.client.handler.CreativeSearchTreeManager;
import fuzs.gamblingstyle.mixin.client.accessor.AbstractContainerScreenAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
@OnlyIn(Dist.CLIENT)
public class ModCreativeModeInventoryScreen extends CreativeModeInventoryScreen {
   private static final ResourceLocation CREATIVE_INVENTORY_LOCATION = new ResourceLocation(GamblingStyle.MOD_ID, "textures/gui/container/creative_inventory/creative_inventory.png");
   private static final int SLOTS_GRID_X = 6;
   private static final int SLOTS_GRID_Y = 7;

   private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
   private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   static final SimpleContainer CONTAINER = new SimpleContainer(SLOTS_GRID_X * SLOTS_GRID_Y);
   private static final Component TRASH_SLOT_TOOLTIP = new TranslatableComponent("inventory.binSlot");
   private static final Component TRASH_SLOT_ALL_TOOLTIP = new TranslatableComponent("inventory.binSlotAll");
   private static final int TEXT_COLOR = -1977417;
   private static int selectedTab = CreativeModeTab.TAB_SEARCH.getId();



   private float scrollOffs;
   private float lastScrollOffs = -1.0F;
   private boolean scrolling;
   private EditBox searchBox;
   private IconButton leftPageButton;
   private IconButton rightPageButton;
   private final TabIconButton[] tabIconButtons = new TabIconButton[11];
   private TabIconButton selectedTabButton;



   private final Slot destroyItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTextInput;
   private static int tabPage = 0;
   private int maxPages = 0;
   private boolean hasClickedOutside;
   private final Set<TagKey<Item>> visibleTags = new HashSet<>();


   private int mouseX;
   private int mouseY;

   public ModCreativeModeInventoryScreen(Player player) {
      super(player);
      ((AbstractContainerScreenAccessor) this).setMenu(new ModItemPickerMenu(player));
      this.destroyItemSlot = this.menu.getSlot(SLOTS_GRID_X * SLOTS_GRID_Y);
      player.containerMenu = this.menu;
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
         this.leftPos = (this.width - this.imageWidth) / 2;
         this.topPos = (this.height - this.imageHeight) / 2;
         this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
         this.initSearchBox();
         this.initTabIconButtons();
         this.initTabPageButtons();
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
         this.tabIconButtons[i] = this.addRenderableWidget(new TabIconButton(this.leftPos + i % 6 * 25, this.topPos - 24 + i / 6 * 192, 22, 22, 324, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
            this.setSelectedTabButton((TabIconButton) button);
         }, (Button button, PoseStack poseStack, int mouseX, int mouseY) -> {
            this.renderTooltip(poseStack, ((TabIconButton) button).getCreativeTab().getDisplayName(), mouseX, mouseY);
         }));
      }
   }

   private void setSelectedTabButton(TabIconButton button) {
      if (this.selectedTabButton != null) {
         this.selectedTabButton.markCreativeTab(false);
      }
      this.selectedTabButton = button;
      this.selectedTabButton.markCreativeTab(true);
      this.selectTab(this.selectedTabButton.getCreativeTab());
   }

   private void initTabPageButtons() {
      this.maxPages = (int) Math.ceil((CreativeModeTab.TABS.length - 12) / 10.0);
      this.leftPageButton = new IconButton(this.leftPos + 125, this.topPos + 169, 11, 20, 368, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
         tabPage = Math.max(tabPage - 1, 0);
         this.updateTabIconButtons();
         this.updatePageButtons();
         this.setSelectedTabButton(this.tabIconButtons[5]);
      });
      this.rightPageButton = new IconButton(this.leftPos + 136, this.topPos + 169, 11, 20, 379, 142, CREATIVE_INVENTORY_LOCATION, 512, 256, button -> {
         tabPage = Math.min(tabPage + 1, this.maxPages);
         this.updateTabIconButtons();
         this.updatePageButtons();
         this.setSelectedTabButton(this.tabIconButtons[5]);
      });
      this.addRenderableWidget(this.leftPageButton);
      this.addRenderableWidget(this.rightPageButton);
      this.updateTabIconButtons();
      this.updatePageButtons();
//      this.setSelectedTabButton(this.tabIconButtons[0]);
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
         if (i == 5) {
            this.tabIconButtons[i].setCreativeTab(CreativeModeTab.TAB_SEARCH);
         } else {
            int tabIndex = start + j++;
            CreativeModeTab tab = tabIndex < CreativeModeTab.TABS.length ? CreativeModeTab.TABS[tabIndex] : null;
            if (tabPage == 0) {
               // skip last tab before search on vanilla tabs screen since vanilla only has 9 tabs with content and to make search stand out
               if (i == 4) {
                  tab = null;
                  j--;
               }
               // skip tab ids for saved hotbars tab (which we don't support) and search (which is hardcoded)
               if (j == 4 || j == 5) {
                  // support for Extended Creative Inventory mod which replaces hotbar tab
                  if (CreativeModeTab.TABS[4] == CreativeModeTab.TAB_HOTBAR || j == 5) {
                     j = 6;
                  }
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
      this.resetScroll();
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
            if (this.menu.canScroll()) {
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

   private void selectTab(@Nullable CreativeModeTab tab) {
      if (tab == null) return;
      int prevSelectedTab = selectedTab;
      selectedTab = tab.getId();
      this.slotColor = tab.getSlotColor();
      this.quickCraftSlots.clear();
      if (prevSelectedTab != tab.getId()) {
         this.searchBox.setValue("");
      }
      this.refreshSearchResults();
      this.resetScroll();
   }

   private void setupMenu() {
      this.menu.items.clear();
      this.menu.slots.clear();
      for (int i = 0; i < SLOTS_GRID_Y; ++i) {
         for (int j = 0; j < SLOTS_GRID_X; ++j) {
            this.addMenuSlot(this.menu, new ModCreativeModeInventoryScreen.CustomCreativeSlot(ModCreativeModeInventoryScreen.CONTAINER, i * SLOTS_GRID_X + j, 12 + j * 18, 29 + i * 18));
         }
      }
      this.addMenuSlot(this.menu, new ModCreativeModeInventoryScreen.CustomCreativeSlot(ModCreativeModeInventoryScreen.CONTAINER, SLOTS_GRID_X * SLOTS_GRID_Y, 120, 10));
      AbstractContainerMenu inventory = this.minecraft.player.inventoryMenu;
      for (int i = 0; i < inventory.slots.size(); ++i) {
         if (i >= 5 && i < 9) {
            this.addMenuSlot(this.menu, new SlotWrapper(inventory.slots.get(i), i, 189, 8 + 18 * (i - 5)));
         } else if (i >= 0 && i < 5) {
            this.addMenuSlot(this.menu, new SlotWrapper(inventory.slots.get(i), i, 0, 0) {
               @Override
               public boolean isActive() {
                  return false;
               }
            });
         } else if (i == 45) {
            this.addMenuSlot(this.menu, new SlotWrapper(inventory.slots.get(i), i, 267, 62));
         } else if (i >= 9 && i < 45) {
            this.addMenuSlot(this.menu, new SlotWrapper(inventory.slots.get(i), i, 156 + 18 * (i - 9) % 9, 84 + 18 * ((i - 9) / 9) + (((i - 9) / 9) > 2 ? 4 : 0)));
         }
      }
   }

   private Slot addMenuSlot(AbstractContainerMenu menu, Slot slot) {
      slot.index = menu.slots.size();
      menu.slots.add(slot);
      return slot;
   }

   @Override
   public boolean mouseScrolled(double p_98527_, double p_98528_, double p_98529_) {
      if (!this.menu.canScroll()) {
         return false;
      } else {
         int i = (this.menu.items.size() + SLOTS_GRID_X - 1) / SLOTS_GRID_X - SLOTS_GRID_Y;
         float f = (float) (p_98529_ / (double) i);
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.scrollTo(this.scrollOffs);
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
         this.scrollTo(this.scrollOffs);
         return true;
      } else {
         return super.mouseDragged(p_98535_, p_98536_, p_98537_, p_98538_, p_98539_);
      }
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(poseStack);
      super.render(poseStack, mouseX, mouseY, partialTicks);
      if (this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, mouseX, mouseY)) {
         this.renderTooltip(poseStack, Screen.hasShiftDown() ? TRASH_SLOT_ALL_TOOLTIP : TRASH_SLOT_TOOLTIP, mouseX, mouseY);
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
      blit(poseStack, this.leftPos + 119, this.topPos + 9, 390, 142, 18, 18, 512, 256);
      // search bar
      blit(poseStack, this.leftPos + 11, this.topPos + 10, 324, 126, 109, 16, 512, 256);
      // item slots
      blit(poseStack, this.leftPos + 11, this.topPos + 28, 324, 0, 109, 126, 512, 256);
      // scroll bar
      blit(poseStack, this.leftPos + 121, this.topPos + 28, 432, 0, 14, 126, 512, 256);
      // scrolling indicator
      int scrollX = this.leftPos + 122;
      int scrollYStart = this.topPos + 29;
      int scrollYEnd = scrollYStart + 126;
      blit(poseStack, scrollX, scrollYStart + (int) ((float) (scrollYEnd - scrollYStart - 17) * this.scrollOffs), 446 + (this.menu.canScroll() ? 0 : 12), 0, 12, 15, 512, 256);
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

   private void scrollTo(float scrollOffs) {
      if (this.lastScrollOffs != scrollOffs) {
         this.menu.scrollTo(scrollOffs);
         this.lastScrollOffs = scrollOffs;
      }
   }

   private void resetScroll() {
      this.lastScrollOffs = -1.0F;
      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
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

   public static class ModItemPickerMenu extends CreativeModeInventoryScreen.ItemPickerMenu {
      public ModItemPickerMenu(Player player) {
         super(player);
         this.slots.clear();
         for (int i = 0; i < SLOTS_GRID_Y; ++i) {
            for (int j = 0; j < SLOTS_GRID_X; ++j) {
               this.addSlot(new ModCreativeModeInventoryScreen.CustomCreativeSlot(ModCreativeModeInventoryScreen.CONTAINER, i * SLOTS_GRID_X + j, 12 + j * 18, 29 + i * 18));
            }
         }
         this.addSlot(new ModCreativeModeInventoryScreen.CustomCreativeSlot(ModCreativeModeInventoryScreen.CONTAINER, SLOTS_GRID_X * SLOTS_GRID_Y, 120, 10));
         AbstractContainerMenu inventory = player.inventoryMenu;
         for (int i = 0; i < inventory.slots.size(); ++i) {
            if (i >= 5 && i < 9) {
               this.addSlot(new SlotWrapper(inventory.slots.get(i), i, 189, 8 + 18 * (i - 5)));
            } else if (i >= 0 && i < 5) {
               this.addSlot(new SlotWrapper(inventory.slots.get(i), i, 0, 0) {
                  @Override
                  public boolean isActive() {
                     return false;
                  }
               });
            } else if (i == 45) {
               this.addSlot(new SlotWrapper(inventory.slots.get(i), i, 267, 62));
            } else if (i >= 9 && i < 45) {
               this.addSlot(new SlotWrapper(inventory.slots.get(i), i, 156 + 18 * ((i - 9) % 9), 84 + 18 * ((i - 9) / 9) + (((i - 9) / 9) > 2 ? 4 : 0)));
            }
         }
      }

      @Override
      public boolean stillValid(Player p_98645_) {
         return true;
      }

      @Override
      public void scrollTo(float scrollOffs) {
         int i = (this.items.size() + SLOTS_GRID_X - 1) / SLOTS_GRID_X - SLOTS_GRID_Y;
         int j = (int)((double)(scrollOffs * (float)i) + 0.5D);
         if (j < 0) {
            j = 0;
         }
         for (int k = 0; k < SLOTS_GRID_Y; ++k) {
            for (int l = 0; l < SLOTS_GRID_X; ++l) {
               int i1 = l + (k + j) * SLOTS_GRID_X;
               if (i1 >= 0 && i1 < this.items.size()) {
                  ModCreativeModeInventoryScreen.CONTAINER.setItem(l + k * SLOTS_GRID_X, this.items.get(i1));
               } else {
                  ModCreativeModeInventoryScreen.CONTAINER.setItem(l + k * SLOTS_GRID_X, ItemStack.EMPTY);
               }
            }
         }
      }

      @Override
      public boolean canScroll() {
         return this.items.size() > SLOTS_GRID_X * SLOTS_GRID_Y;
      }

      @Override
      public ItemStack quickMoveStack(Player p_98650_, int p_98651_) {
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
   }

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
