package com.fuzs.gamblingstyle.client.gui;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.client.gui.core.IGuiExtension;
import com.fuzs.gamblingstyle.client.gui.core.ITooltipButton;
import com.fuzs.gamblingstyle.client.gui.data.TradingRecipe;
import com.fuzs.gamblingstyle.client.gui.data.TradingRecipeList;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiTradingBook extends Gui implements IGuiExtension {

    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation(GamblingStyle.MODID, "textures/gui/container/merchant_book.png");
    public static final int MAX_BUTTONS = 6;

    private Minecraft mc;
    private final int xSize = 112;
    private final int ySize = 166;
    public int hoveredSlot;
    private ITooltipButton hoveredButton;
    private final GuiButtonTradingRecipe[] tradeButtons = new GuiButtonTradingRecipe[MAX_BUTTONS];
    private GuiTextField searchField;
    private final GuiButtonFilter filterButton;
    private String lastSearch = "";
    private int guiLeft;
    private int guiTop;
    private boolean requiresRefresh;
    private TradingRecipeList tradingRecipeList;
    // Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
    private float currentScroll;
    private int scrollPosition;
    private boolean isScrolling;
    private boolean wasClicking;
    private GuiButton clickedButton;
    private int selectedTradingRecipe;
    private boolean clearSearch;
    private int timesInventoryChanged;

    public GuiTradingBook(ITradingInfo.FilterMode filterMode) {

        this.filterButton = new GuiButtonFilter(this.tradeButtons.length, this.guiLeft + 94, this.guiTop + 8, filterMode);
        for (int i = 0; i < this.tradeButtons.length; ++i) {

            this.tradeButtons[i] = new GuiButtonTradingRecipe(i, this.guiLeft + 10, this.guiTop + 24 + 22 * i);
        }
    }

    @Override
    public void initGui(Minecraft mc, int width, int height) {

        this.mc = mc;
        this.requiresRefresh = true;
        Keyboard.enableRepeatEvents(true);
        this.guiLeft = (width - this.xSize) / 2 - 88;
        this.guiTop = (height - this.ySize) / 2;
        this.timesInventoryChanged = this.mc.player.inventory.getTimesChanged();
        this.initSearchField();

        this.filterButton.setPosition(this.guiLeft + 94, this.guiTop + 8);
        for (int i = 0; i < this.tradeButtons.length; i++) {

            this.tradeButtons[i].setPosition(this.guiLeft + 10, this.guiTop + 24 + 22 * i);
        }

        if (!this.lastSearch.isEmpty()) {

            this.lastSearch = "";
            this.invalidate(true);
        }
    }

    private void initSearchField() {

        this.searchField = new GuiTextField(0, this.mc.fontRenderer, this.guiLeft + 9, this.guiTop + 9, 76, this.mc.fontRenderer.FONT_HEIGHT);
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setTextColor(16777215);
    }

    @Override
    public void onGuiClosed() {

        Keyboard.enableRepeatEvents(false);
    }

    public void setSelectedTradingRecipe(int recipeIndex) {

        if (this.tradingRecipeList != null) {

            this.tradingRecipeList.get(this.selectedTradingRecipe).setSelected(false);
            this.selectedTradingRecipe = recipeIndex;
            this.tradingRecipeList.get(this.selectedTradingRecipe).setSelected(true);
            this.requiresRefresh = true;
        } else {

            this.selectedTradingRecipe = recipeIndex;
        }
    }

    @Override
    public void updateScreen(MerchantRecipeList merchantrecipelist, ContainerVillager container) {

        if (this.timesInventoryChanged != this.mc.player.inventory.getTimesChanged()) {

            this.countTradeMaterials(container);
            this.timesInventoryChanged = this.mc.player.inventory.getTimesChanged();
        }

        if (this.clearSearch) {

            this.searchField.setCursorPositionEnd();
            this.searchField.setSelectionPos(0);
        }

        if (this.requiresRefresh) {

            this.requiresRefresh = false;
            if (this.tradingRecipeList != null && this.tradingRecipeList.size() == merchantrecipelist.size()) {

                this.updateVisibleTrades(merchantrecipelist);
            }
        }
    }

    public void setRecipes(MerchantRecipeList merchantrecipelist, ContainerVillager container, byte[] favorites) {

        this.tradingRecipeList = new TradingRecipeList(merchantrecipelist);
        this.tradingRecipeList.get(this.selectedTradingRecipe).setSelected(true);
        for (byte favorite : favorites) {

            if (favorite < this.tradingRecipeList.size()) {

                this.tradingRecipeList.get(favorite).favorite();
            }
        }

        this.countTradeMaterials(container);
        this.invalidate(true);
    }

    public void countTradeMaterials(ContainerVillager container) {

        if (this.tradingRecipeList != null) {

            this.tradingRecipeList.countTradeMaterials(container);
            this.requiresRefresh = true;
        }
    }

    private void updateVisibleTrades(MerchantRecipeList merchantrecipelist) {

        int scrollPosition = this.scrollPosition;
        for (GuiButtonTradingRecipe tradeButton : this.tradeButtons) {

            tradeButton.visible = false;
            for (int i = scrollPosition; i < this.tradingRecipeList.size(); i++) {

                TradingRecipe tradingRecipe = this.tradingRecipeList.get(i);
                if (tradingRecipe.isVisible()) {

                    tradeButton.setContents(i, tradingRecipe, merchantrecipelist.get(i).isRecipeDisabled());
                    scrollPosition = i + 1;
                    tradeButton.visible = true;
                    break;
                } else {

                    scrollPosition++;
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 100.0F);
        this.mc.getTextureManager().bindTexture(RECIPE_BOOK);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderScrollBar(mouseX, mouseY);
        RenderHelper.disableStandardItemLighting();
        this.renderButtons(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
    }

    private void renderScrollBar(int mouseX, int mouseY) {

        if (this.tradingRecipeList == null) {

            return;
        }

        boolean isClicked = Mouse.isButtonDown(0);
        int recipes = this.tradingRecipeList.getActiveRecipeAmount();
        int barPosition = (int) (136.0F / (float) Math.sqrt((float) Math.max(recipes - MAX_BUTTONS + 1, 1)));
        int startX = this.guiLeft + 98;
        int startY = this.guiTop + 21;
        int endX = startX + 6;
        int endY = startY + 136;
        boolean scrollable = recipes > MAX_BUTTONS;
        this.mc.getTextureManager().bindTexture(RECIPE_BOOK);
        this.drawTexturedModalRect(startX, startY + (int) ((float) (endY - startY - barPosition) * this.currentScroll), scrollable ? 196 : 202, 0, 6, barPosition);
        this.drawTexturedModalRect(startX, startY + barPosition + (int) ((float) (endY - startY - barPosition) * this.currentScroll), scrollable ? 196 : 202, 136, 6, 1); // end of stripe
        if (!this.wasClicking && isClicked && mouseX >= startX && mouseY >= startY && mouseX < endX && mouseY < endY + 1) {

            this.isScrolling = scrollable;
        }

        if (!isClicked) {

            this.isScrolling = false;
        }

        this.wasClicking = isClicked;
        if (this.isScrolling) {

            this.currentScroll = ((float) (mouseY - startY) - 7.5F) / ((float) (endY - startY) - 15.0F);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            this.updateScrollPosition();
        }
    }

    private void renderButtons(int mouseX, int mouseY, float partialTicks) {

        this.hoveredButton = null;
        this.filterButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        for (GuiButtonTradingRecipe tradeButton : this.tradeButtons) {

            tradeButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
            if (tradeButton.isMouseOver() && tradeButton.visible) {

                this.hoveredButton = tradeButton;
            }
        }

        if (this.hoveredButton == null && this.filterButton.isMouseOver()) {

            this.hoveredButton = this.filterButton;
        }
    }

    public void renderHoveredTooltip(int mouseX, int mouseY) {

        if (this.mc.currentScreen != null && this.hoveredButton != null) {

            List<String> tooltip = this.hoveredButton.getToolTip(this.mc.currentScreen, mouseX, mouseY);
            if (tooltip != null && this.mc.player.inventory.getItemStack().isEmpty()) {

                this.mc.currentScreen.drawHoveringText(tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (this.searchField.mouseClicked(mouseX, mouseY, mouseButton)) {

            return true;
        }

        if (mouseButton == 0 || mouseButton == 1) {

            if (this.filterButton.mousePressed(this.mc, mouseX, mouseY)) {

                this.filterButton.cycleFilterMode(this.getFavorites() == 0);
                this.filterButton.playPressSound(this.mc.getSoundHandler());
                this.invalidate(true);

                return true;
            }
        }

        return false;
    }

    public int mouseClickedTradeButtons(int mouseX, int mouseY, int mouseButton) {

        if (mouseButton == 0 || mouseButton == 1) {

            for (GuiButtonTradingRecipe tradeButton : this.tradeButtons) {

                if (tradeButton.mousePressedOnFavorite(mouseX, mouseY)) {

                    this.tradingRecipeList.get(tradeButton.getRecipeId()).toggleFavorite();
                    if (this.getFavorites() == 0 && this.getCurrentFilterMode() == ITradingInfo.FilterMode.FAVORITES) {

                        this.filterButton.cycleFilterMode(false);
                        this.invalidate(true);
                    } else {

                        this.invalidate(false);
                    }

                    return -1;
                } else if (tradeButton.mousePressed(this.mc, mouseX, mouseY)) {

                    this.clearSearch = true;
                    this.clickedButton = tradeButton;
                    tradeButton.playPressSound(this.mc.getSoundHandler());

                    return tradeButton.getRecipeId();
                }
            }
        }

        return -1;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

        if (this.clickedButton != null && state == 0) {

            this.clickedButton.mouseReleased(mouseX, mouseY);
            this.clickedButton = null;
        }
    }

    @Override
    public void handleMouseInput() {

        int scrollAmount = Mouse.getEventDWheel();
        if (scrollAmount != 0 && this.tradingRecipeList != null) {

            int recipes = this.tradingRecipeList.getActiveRecipeAmount();
            if (recipes > MAX_BUTTONS) {

                if (scrollAmount > 0) {

                    scrollAmount = 1;
                }

                if (scrollAmount < 0) {

                    scrollAmount = -1;
                }

                this.currentScroll = (float) ((double) this.currentScroll - (double) scrollAmount / (double) recipes);
                this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
                this.updateScrollPosition();
            }
        }
    }

    public boolean hasRecipeContents(int id) {

        if (this.tradingRecipeList != null && this.tradingRecipeList.size() > id) {

            return this.tradingRecipeList.get(id).hasRecipeContents();
        }

        return false;
    }

    @Override
    public boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize) {

        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + xSize || mouseY >= guiTop + ySize;
        boolean flag1 = guiLeft - this.xSize < mouseX && mouseX < guiLeft && guiTop < mouseY && mouseY < guiTop + ySize;

        return flag && !flag1;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {

        if (this.isKeyValid(keyCode)) {

            return false;
        }

        if (this.clearSearch) {

            this.searchField.setText("");
            this.clearSearch = false;
        }

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {

            String searchQuery = this.searchField.getText();
            if (!searchQuery.equals(this.lastSearch) && this.tradingRecipeList != null) {

                this.lastSearch = searchQuery;
                this.invalidate(true);
            }

            return true;
        }

        return false;
    }

    private void invalidate(boolean resetScroll) {

        this.tradingRecipeList.search(this.mc, this.lastSearch, this.getCurrentFilterMode());
        if (resetScroll) {

            this.currentScroll = 0.0F;
            this.updateScrollPosition();
        }

        this.requiresRefresh = true;
    }

    private boolean isKeyValid(int keyCode) {

        if (this.mc.player.inventory.getItemStack().isEmpty() && this.hoveredSlot > 0) {

            GameSettings settings = this.mc.gameSettings;
            for (int i = 0; i < 9; ++i) {

                if (settings.keyBindsHotbar[i].isActiveAndMatches(keyCode)) {

                    return true;
                }
            }

            if (this.hoveredSlot > 1) {

                return settings.keyBindDrop.isActiveAndMatches(keyCode);
            }
        }

        return false;
    }

    private void updateScrollPosition() {

        if (this.tradingRecipeList != null) {

            int activeRecipes = this.tradingRecipeList.getActiveRecipeAmount();
            int scrollAmount = (int) ((double) (this.currentScroll * (float) Math.max(activeRecipes - MAX_BUTTONS, 0)) + 0.5);
            scrollAmount = Math.max(0, scrollAmount);

            int[] activeTradeIndices = this.getActiveTradeIndices(activeRecipes);
            if (scrollAmount < activeTradeIndices.length && this.scrollPosition != activeTradeIndices[scrollAmount]) {

                this.scrollPosition = activeTradeIndices[scrollAmount];
                this.requiresRefresh = true;
            }
        }
    }

    private int[] getActiveTradeIndices(int activeRecipes) {

        int[] activeTradeIndices;
        if (activeRecipes < this.tradingRecipeList.size()) {

            activeTradeIndices = IntStream.range(0, this.tradingRecipeList.size())
                    .map(recipeIndex -> this.tradingRecipeList.get(recipeIndex).isVisible() ? recipeIndex : -1)
                    .filter(recipeIndex -> recipeIndex >= 0)
                    .toArray();
        } else {

            activeTradeIndices = IntStream.range(0, this.tradingRecipeList.size())
                    .toArray();
        }

        return activeTradeIndices;
    }

    public ITradingInfo.FilterMode getCurrentFilterMode() {

        return this.filterButton.getFilterMode();
    }

    public byte[] getFavoriteTrades() {

        byte[] favorites = new byte[this.getFavorites()];
        for (int i = 0, j = 0; i < favorites.length; j++) {

            if (this.tradingRecipeList.get(j).isFavorite()) {

                favorites[i++] = (byte) j;
            }
        }

        return favorites;
    }

    private int getFavorites() {

        return (int) this.tradingRecipeList.stream().filter(TradingRecipe::isFavorite).count();
    }

}