package com.fuzs.gamblingstyle.client.gui;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.network.message.CMoveIngredientsMessage;
import com.fuzs.gamblingstyle.network.message.CSelectedRecipeMessage;
import com.fuzs.gamblingstyle.network.message.CSyncTradingInfoMessage;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;

@SuppressWarnings("NullableProblems")
@SideOnly(Side.CLIENT)
public class GuiVillager extends GuiContainer {

    private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation(GamblingStyle.MODID, "textures/gui/container/merchant.png");

    private final IMerchant merchant;
    private final EntityLivingBase traderEntity;
    private final ITextComponent windowTitle;
    private int currentRecipeIndex;
    private final byte[] favoriteTrades;

    private final GuiTradingBook tradingBookGui;
    private final GhostTrade ghostTrade;

    public GuiVillager(InventoryPlayer playerInventory, IMerchant merchant, EntityLivingBase traderEntity, int currentRecipeIndex, ITradingInfo.FilterMode filterMode, byte[] favoriteTrades) {

        super(new ContainerVillager(playerInventory, merchant, traderEntity.world));
        this.merchant = merchant;
        this.traderEntity = traderEntity;
        this.windowTitle = merchant.getDisplayName();
        this.currentRecipeIndex = currentRecipeIndex;
        this.tradingBookGui = new GuiTradingBook(filterMode);
        this.ghostTrade = new GhostTrade();
        this.favoriteTrades = favoriteTrades;
        // TODO is this necessary?
        this.sendSelectedRecipe(false);
    }

    @Override
    public void initGui() {

        super.initGui();
        // trading book might be open or not
        this.guiLeft = (this.width - this.xSize) / 2 + 57;
        this.tradingBookGui.initGui(this.mc, this.width, this.height);
        // not that this should every change, but it's updated in the super method
        this.ghostTrade.initGui(this.mc);
    }

    @Override
    public void onGuiClosed() {

        this.tradingBookGui.onGuiClosed();
        NetworkHandler.get().sendToServer(new CSyncTradingInfoMessage(this.traderEntity.getEntityId(), this.currentRecipeIndex, this.tradingBookGui.getCurrentFilterMode(), this.tradingBookGui.getFavoriteTrades()));
        super.onGuiClosed();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        String windowTitle = this.windowTitle.getUnformattedText();
        this.fontRenderer.drawString(windowTitle, this.xSize / 2 - this.fontRenderer.getStringWidth(windowTitle) / 2 + 23, 6, 4210752);
        this.fontRenderer.drawString(new TextComponentTranslation("container.inventory").getUnformattedText(), 62, this.ySize - 96 + 2, 4210752);
    }

    @Override
    public void updateScreen() {

        super.updateScreen();
        MerchantRecipeList merchantRecipes = this.merchant.getRecipes(this.mc.player);
        if (merchantRecipes != null) {

            this.tradingBookGui.updateScreen(merchantRecipes, (ContainerVillager) this.inventorySlots);
        }

        Slot hoveredSlot = this.getSlotUnderMouse();
        this.tradingBookGui.hoveredSlot = hoveredSlot != null ? hoveredSlot.getHasStack() ? 2 : 1 : 0;
        if (((ContainerVillager) this.inventorySlots).areSlotsFilled()) {

            this.ghostTrade.clear();
        }
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop) {

        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + this.xSize || mouseY >= guiTop + this.ySize;

        return this.tradingBookGui.hasClickedOutside(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            int selectedRecipe = this.currentRecipeIndex;
            MerchantRecipe merchantrecipe = merchantrecipelist.get(selectedRecipe);
            if (merchantrecipe.isRecipeDisabled()) {

                this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 32, 212, 0, 28, 21);
            }
        }

        GuiInventory.drawEntityOnScreen(this.guiLeft + 33, this.guiTop + 75, 30, this.guiLeft + 33 - mouseX,
                this.guiTop + 75 - 50 - mouseY, this.traderEntity);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        if (!this.tradingBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {

            int recipeIndex = this.tradingBookGui.mouseClickedTradeButtons(mouseX, mouseY, mouseButton);
            if (recipeIndex != -1) {

                this.updateSelectedRecipe(recipeIndex, mouseButton == 1);
            } else {

                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    private void updateSelectedRecipe(int recipeIndex, boolean skipMove) {

        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            MerchantRecipe recipe = merchantrecipelist.get(recipeIndex);
            boolean isNotSelected = this.currentRecipeIndex != recipeIndex;
            boolean hasIngredients = this.tradingBookGui.hasRecipeContents(recipeIndex);
            boolean isDisabled = recipe.isRecipeDisabled();
            if (isNotSelected) {

                this.currentRecipeIndex = recipeIndex;
                this.sendSelectedRecipe(!hasIngredients || isDisabled);
            }

            if (hasIngredients) {

                this.ghostTrade.clear();
                if (!isDisabled) {

                    this.moveRecipeIngredients(isNotSelected, GuiScreen.isShiftKeyDown(), skipMove);
                }
            } else {

                this.ghostTrade.setRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell());
                if (((ContainerVillager) this.inventorySlots).areSlotsFilled()) {

                    this.sendSelectedRecipe(true);
                }
            }
        }
    }

    private void sendSelectedRecipe(boolean clearSlots) {

        ((ContainerVillager) this.inventorySlots).setCurrentRecipeIndex(this.currentRecipeIndex);
        if (clearSlots) {

            ((ContainerVillager) this.inventorySlots).clearTradingSlots();
        }

        this.tradingBookGui.setSelectedTradingRecipe(this.currentRecipeIndex);
        NetworkHandler.get().sendToServer(new CSelectedRecipeMessage(this.currentRecipeIndex, clearSlots));
    }

    private void moveRecipeIngredients(boolean clear, boolean quickMove, boolean skipMove) {

        ((ContainerVillager) this.inventorySlots).handleClickedButtonItems(this.currentRecipeIndex, clear, quickMove, skipMove);
        NetworkHandler.get().sendToServer(new CMoveIngredientsMessage(this.currentRecipeIndex, clear, quickMove, skipMove));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {

        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.tradingBookGui.countTradeMaterials((ContainerVillager) this.inventorySlots);
        if (slotIn != null && slotId <= 2) {

            MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
            if (merchantrecipelist != null && merchantrecipelist.size() > this.currentRecipeIndex) {

                if (!merchantrecipelist.get(this.currentRecipeIndex).hasSecondItemToBuy() && slotId == 1) {

                    return;
                }

                this.ghostTrade.clear();
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {

        this.tradingBookGui.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {

        this.tradingBookGui.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.drawDefaultBackground();
        this.tradingBookGui.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.ghostTrade.render(this.guiLeft, this.guiTop);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            MerchantRecipe merchantrecipe = merchantrecipelist.get(this.currentRecipeIndex);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            if (merchantrecipe.isRecipeDisabled() && this.isPointInRegion(97, 32, 28, 21, mouseX, mouseY)) {

                this.drawHoveringText(new TextComponentTranslation("merchant.deprecated").getUnformattedText(), mouseX, mouseY);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.tradingBookGui.renderHoveredTooltip(mouseX, mouseY);
        this.ghostTrade.renderHoveredTooltip(mouseX, mouseY, this.guiLeft, this.guiTop);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (!this.tradingBookGui.keyTyped(typedChar, keyCode)) {

            super.keyTyped(typedChar, keyCode);
        }
    }

    public void setMerchantRecipes(@Nullable MerchantRecipeList merchantRecipes) {

        this.merchant.setRecipes(merchantRecipes);
        if (merchantRecipes != null) {

            this.tradingBookGui.setRecipes(merchantRecipes, (ContainerVillager) this.inventorySlots, this.favoriteTrades);
        }
    }

}