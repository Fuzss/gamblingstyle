package fuzs.gamblingstyle.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.gamblingstyle.GamblingStyle;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ConstantConditions")
public class ModRecipeBookComponent extends RecipeBookComponent {
   private static final Component SEARCH_HINT = (new TranslatableComponent("gui.recipebook.search_hint")).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
   private static final Component ONLY_CRAFTABLES_TOOLTIP = new TranslatableComponent("gui.recipebook.toggleRecipes.craftable");
   private static final Component ALL_RECIPES_TOOLTIP = new TranslatableComponent("gui.recipebook.toggleRecipes.all");

   protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation(GamblingStyle.MOD_ID, "textures/gui/recipe_book.png");
   public static final int IMAGE_WIDTH = 147;
   public static final int IMAGE_HEIGHT = 166;
   private static final int OFFSET_X_POSITION = 86;
   private static final int RECIPES_GRID_X = 6;
   private static final int RECIPES_GRID_Y = 7;

   protected final GhostRecipe ghostRecipe = new GhostRecipe();
   protected RecipeBookMenu<?> menu;
   protected Minecraft minecraft;
   @Nullable
   private Screen screen;
   @Nullable
   private EditBox searchBox;
   private String lastSearch = "";
   private ClientRecipeBook book;


   private boolean widthTooNarrow;
   private int leftPos;
   private int topPos;
   protected List<Recipe<?>> recipes;
   private final Recipe<?>[] currentRecipes = new Recipe<?>[RECIPES_GRID_X * RECIPES_GRID_Y];
   private float scrollOffs;
   private boolean scrolling;


   private final RecipeBookPage recipeBookPage = new RecipeBookPage();
   private final StackedContents stackedContents = new StackedContents();
   private boolean ignoreTextInput;
   private boolean visible;
   private ContainerListener listener;

   public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow, RecipeBookMenu<?> menu) {
      this.minecraft = minecraft;
      this.screen = minecraft.screen;
      this.widthTooNarrow = widthTooNarrow;
      this.leftPos = (width - IMAGE_WIDTH) / 2 - (widthTooNarrow ? 0 : OFFSET_X_POSITION);
      this.topPos = (height - IMAGE_HEIGHT) / 2;
      this.menu = menu;
      this.book = minecraft.player.getRecipeBook();
      this.visible = this.isVisibleAccordingToBookData();
      if (this.visible) {
         this.initVisuals();
      }
      this.updateSlotListener();
      minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.sendUpdateSettings();
   }

   private void updateSlotListener() {
      this.menu.removeSlotListener(this.listener);
      this.listener = new RecipeBookListener(this);
      this.menu.addSlotListener(this.listener);
   }

   public void initVisuals() {
      this.stackedContents.clear();
      this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      this.initSearchBox(this.leftPos, this.topPos);
      this.recipeBookPage.init(this.minecraft, this.leftPos, this.topPos);
      this.recipeBookPage.addListener(this);
      this.updateCollections();
   }

   private void initSearchBox(int i, int j) {
      String s = this.searchBox != null ? this.searchBox.getValue() : "";
      this.searchBox = new EditBox(this.minecraft.font, i + 25, j + 14, 80, 9 + 5, new TranslatableComponent("itemGroup.search"));
      this.searchBox.setMaxLength(50);
      this.searchBox.setBordered(false);
      this.searchBox.setVisible(true);
      this.searchBox.setTextColor(16777215);
      this.searchBox.setValue(s);
   }

   public boolean changeFocus(boolean p_100372_) {
      return false;
   }

   public void removed() {
      this.menu.removeSlotListener(this.listener);
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public int updateScreenPosition(int width, int imageWidth) {
      int i;
      if (this.isVisible() && !this.widthTooNarrow) {
         i = 177 + (width - imageWidth - 200) / 2;
      } else {
         i = (width - imageWidth) / 2;
      }

      return i;
   }

   public void toggleVisibility() {
      this.setVisible(!this.isVisible());
   }

   public boolean isVisible() {
      return this.visible;
   }

   private boolean isVisibleAccordingToBookData() {
      return this.book.isOpen(this.menu.getRecipeBookType());
   }

   protected void setVisible(boolean p_100370_) {
      if (p_100370_) {
         this.initVisuals();
      }

      this.visible = p_100370_;
      this.book.setOpen(this.menu.getRecipeBookType(), p_100370_);
      if (!p_100370_) {
         this.recipeBookPage.setInvisible();
      }

      this.sendUpdateSettings();
   }

   public void slotClicked(@Nullable Slot slot) {
      if (slot != null && slot.index < this.menu.getSize()) {
         this.ghostRecipe.clear();
         if (this.isVisible()) {
            this.updateStackedContents();
         }
      }
   }

   private void updateCollections() {
      List<RecipeCollection> list = this.getRecipeBookCollection();
      list.forEach((p_100381_) -> {
         p_100381_.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book);
      });
      List<RecipeCollection> list1 = Lists.newArrayList(list);
      list1.removeIf((p_100368_) -> {
         return !p_100368_.hasKnownRecipes();
      });
      list1.removeIf((p_100360_) -> {
         return !p_100360_.hasFitting();
      });
      String s = this.searchBox.getValue();
      if (!s.isEmpty()) {
         ObjectSet<RecipeCollection> objectset = new ObjectLinkedOpenHashSet<>(this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
         list1.removeIf((p_100334_) -> {
            return !objectset.contains(p_100334_);
         });
      }
      this.recipes = this.collectAllRecipes(list1);
      this.scrollOffs = 0.0F;
      this.scrollTo(0.0F);
   }

   private List<Recipe<?>> collectAllRecipes(List<RecipeCollection> recipeCollections) {
      List<Recipe<?>> craftable = Lists.newArrayList();
      List<Recipe<?>> uncraftable = Lists.newArrayList();
      for (RecipeCollection collection : recipeCollections) {
         craftable.addAll(collection.getDisplayRecipes(true));
         uncraftable.addAll(collection.getDisplayRecipes(false));
      }
      return new ImmutableList.Builder<Recipe<?>>().addAll(craftable).addAll(uncraftable).build();
   }

   private List<RecipeCollection> getRecipeBookCollection() {
      List<RecipeBookCategories> categories = RecipeBookCategories.getCategories(this.menu.getRecipeBookType());
      for (RecipeBookCategories category : categories) {
         if (RecipeBookCategories.AGGREGATE_CATEGORIES.containsKey(category)) {
            return this.book.getCollection(category);
         }
      }
      return Collections.emptyList();
   }

   public void scrollTo(float scrollOffs) {
      int i = (this.recipes.size() + RECIPES_GRID_X - 1) / RECIPES_GRID_X - RECIPES_GRID_Y;
      int j = (int)((double)(scrollOffs * (float)i) + 0.5D);
      if (j < 0) {
         j = 0;
      }

      for(int k = 0; k < RECIPES_GRID_Y; ++k) {
         for(int l = 0; l < RECIPES_GRID_X; ++l) {
            int i1 = l + (k + j) * RECIPES_GRID_X;
            if (i1 >= 0 && i1 < this.recipes.size()) {
               this.currentRecipes[l + k * RECIPES_GRID_X] = this.recipes.get(i1);
            } else {
               this.currentRecipes[l + k * RECIPES_GRID_X] = null;
            }
         }
      }

   }

   public boolean canScroll() {
      return this.recipes.size() > RECIPES_GRID_X * RECIPES_GRID_Y;
   }

   public void tick() {
      boolean flag = this.isVisibleAccordingToBookData();
      if (this.isVisible() != flag) {
         this.setVisible(flag);
      }

      if (this.isVisible()) {
         this.searchBox.tick();
      }
   }

   public void updateStackedContents() {
      this.stackedContents.clear();
      this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      this.updateCollections();
   }

   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
      if (this.isVisible()) {
         poseStack.pushPose();
         poseStack.translate(0.0D, 0.0D, 100.0D);
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         // book background
         this.blit(poseStack, this.leftPos, this.topPos, 0, 0, 147, 166);
         // search bar
         this.blit(poseStack, this.leftPos + 11, this.topPos + 10, 0, 166, 101, 16);
         // item slots
         this.blit(poseStack, this.leftPos + 11, this.topPos + 28, 147, 0, 109, 126);
         // scroll bar
         this.blit(poseStack, this.leftPos + 121, this.topPos + 28, 201, 126, 14, 126);
         // scrolling indicator
         int scrollX = this.leftPos + 122;
         int scrollYStart = this.topPos + 29;
         int scrollYEnd = scrollYStart + 126;
         this.blit(poseStack, scrollX, scrollYStart + (int) ((float) (scrollYEnd - scrollYStart - 17) * this.scrollOffs), 215 + (this.canScroll() ? 0 : 12), 126, 12, 15);
         this.searchBox.render(poseStack, mouseX, mouseY, partialTicks);
         this.recipeBookPage.render(poseStack, this.leftPos, this.topPos, mouseX, mouseY, partialTicks);
         poseStack.popPose();
      }
   }

   public void renderTooltip(PoseStack p_100362_, int p_100363_, int p_100364_, int p_100365_, int p_100366_) {
      if (this.isVisible()) {
         this.recipeBookPage.renderTooltip(p_100362_, p_100365_, p_100366_);
         if (this.filterButton.isHoveredOrFocused()) {
            Component component = this.getFilterButtonTooltip();
            if (this.minecraft.screen != null) {
               this.minecraft.screen.renderTooltip(p_100362_, component, p_100365_, p_100366_);
            }
         }

         this.renderGhostRecipeTooltip(p_100362_, p_100363_, p_100364_, p_100365_, p_100366_);
      }
   }

   private Component getFilterButtonTooltip() {
      return this.filterButton.isStateTriggered() ? this.getRecipeFilterName() : ALL_RECIPES_TOOLTIP;
   }

   protected Component getRecipeFilterName() {
      return ONLY_CRAFTABLES_TOOLTIP;
   }

   private void renderGhostRecipeTooltip(PoseStack p_100375_, int p_100376_, int p_100377_, int p_100378_, int p_100379_) {
      ItemStack itemstack = null;

      for(int i = 0; i < this.ghostRecipe.size(); ++i) {
         GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
         int j = ghostrecipe$ghostingredient.getX() + p_100376_;
         int k = ghostrecipe$ghostingredient.getY() + p_100377_;
         if (p_100378_ >= j && p_100379_ >= k && p_100378_ < j + 16 && p_100379_ < k + 16) {
            itemstack = ghostrecipe$ghostingredient.getItem();
         }
      }

      if (itemstack != null && this.minecraft.screen != null) {
         this.minecraft.screen.renderComponentTooltip(p_100375_, this.minecraft.screen.getTooltipFromItem(itemstack), p_100378_, p_100379_, itemstack);
      }

   }

   public void renderGhostRecipe(PoseStack p_100323_, int p_100324_, int p_100325_, boolean p_100326_, float p_100327_) {
      this.ghostRecipe.render(p_100323_, this.minecraft, p_100324_, p_100325_, p_100326_, p_100327_);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.recipeBookPage.mouseClicked(mouseX, mouseY, button, this.leftPos, this.topPos, 147, 166)) {
            Recipe<?> recipe = this.recipeBookPage.getLastClickedRecipe();
            RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
            if (recipe != null && recipecollection != null) {
               if (!recipecollection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
                  return false;
               }

               this.ghostRecipe.clear();
               this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipe, Screen.hasShiftDown());
               if (this.widthTooNarrow) {
                  this.setVisible(false);
               }
            }

            return true;
         } else if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
         } else if (this.filterButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
         } else {
            if (button == 0) {
               if (this.insideScrollbar(mouseX, mouseY)) {
                  this.scrolling = this.canScroll();
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean insideScrollbar(double mouseX, double mouseY) {
      int fromX = this.leftPos + 122;
      int fromY = this.topPos + 29;
      int toX = fromX + 14;
      int toY = fromY + 126;
      return mouseX >= (double)fromX && mouseY >= (double)fromY && mouseX < (double)toX && mouseY < (double)toY;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.scrolling = false;
      }
      return false;
   }

   public boolean mouseScrolled(double p_98527_, double p_98528_, double p_98529_) {
      if (!this.canScroll()) {
         return false;
      } else {
         int i = (this.recipes.size() + RECIPES_GRID_X - 1) / RECIPES_GRID_X - RECIPES_GRID_Y;
         float f = (float)(p_98529_ / (double)i);
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.scrollTo(this.scrollOffs);
         return true;
      }
   }

   public boolean mouseDragged(double p_98535_, double p_98536_, int p_98537_, double p_98538_, double p_98539_) {
      if (this.scrolling) {
         int scrollTop = this.topPos + 29;
         int scrollBottom = scrollTop + 126;
         this.scrollOffs = ((float)p_98536_ - (float)scrollTop - 7.5F) / ((float)(scrollBottom - scrollTop) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.scrollTo(this.scrollOffs);
         return true;
      } else {
         return super.mouseDragged(p_98535_, p_98536_, p_98537_, p_98538_, p_98539_);
      }
   }

   public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int imageWidth, int imageHeight, int button) {
      if (!this.isVisible()) {
         return true;
      } else {
         boolean flag1 = (double)(leftPos - IMAGE_WIDTH) < mouseX && mouseX < (double)leftPos && (double)topPos < mouseY && mouseY < (double)(topPos + IMAGE_HEIGHT);
         return !flag1;
      }
   }

   public boolean keyPressed(int p_100306_, int p_100307_, int p_100308_) {
      this.ignoreTextInput = false;
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (p_100306_ == 256 && this.widthTooNarrow) {
            this.setVisible(false);
            return true;
         } else if (this.searchBox.keyPressed(p_100306_, p_100307_, p_100308_)) {
            this.checkSearchStringUpdate();
            return true;
         } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && p_100306_ != 256) {
            return true;
         } else if (this.minecraft.options.keyChat.matches(p_100306_, p_100307_) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocus(true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean keyReleased(int p_100356_, int p_100357_, int p_100358_) {
      this.ignoreTextInput = false;
      return false;
   }

   public boolean charTyped(char p_100291_, int p_100292_) {
      if (this.ignoreTextInput) {
         return false;
      } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.searchBox.charTyped(p_100291_, p_100292_)) {
            this.checkSearchStringUpdate();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isMouseOver(double p_100353_, double p_100354_) {
      return false;
   }

   private void checkSearchStringUpdate() {
      String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
      if (!s.equals(this.lastSearch)) {
         this.updateCollections();
         this.lastSearch = s;
      }

   }

   public void recipesUpdated() {
      if (this.isVisible()) {
         this.updateCollections();
      }
   }

   public void recipesShown(List<Recipe<?>> p_100344_) {
      for(Recipe<?> recipe : p_100344_) {
         this.minecraft.player.removeRecipeHighlight(recipe);
      }

   }

   public void setupGhostRecipe(Recipe<?> p_100316_, List<Slot> p_100317_) {
      ItemStack itemstack = p_100316_.getResultItem();
      this.ghostRecipe.setRecipe(p_100316_);
      this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (p_100317_.get(0)).x, (p_100317_.get(0)).y);
      this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), p_100316_, p_100316_.getIngredients().iterator(), 0);
   }

   public void addItemToSlot(Iterator<Ingredient> p_100338_, int p_100339_, int p_100340_, int p_100341_, int p_100342_) {
      Ingredient ingredient = p_100338_.next();
      if (!ingredient.isEmpty()) {
         Slot slot = this.menu.slots.get(p_100339_);
         this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
      }

   }

   protected void sendUpdateSettings() {
      if (this.minecraft.getConnection() != null) {
         RecipeBookType recipebooktype = this.menu.getRecipeBookType();
         boolean flag = this.book.getBookSettings().isOpen(recipebooktype);
         this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(recipebooktype, flag, false));
      }

   }

   public NarrationPriority narrationPriority() {
      return this.visible ? NarrationPriority.HOVERED : NarrationPriority.NONE;
   }

   public void updateNarration(NarrationElementOutput p_170046_) {
      List<NarratableEntry> list = Lists.newArrayList();
      this.recipeBookPage.listButtons((p_170049_) -> {
         if (p_170049_.isActive()) {
            list.add(p_170049_);
         }

      });
      list.add(this.searchBox);
      list.add(this.filterButton);
      list.addAll(this.tabButtons);
      Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, null);
      if (screen$narratablesearchresult != null) {
         screen$narratablesearchresult.entry.updateNarration(p_170046_.nest());
      }

   }
}
