package com.fuzs.gamblingstyle.client.gui;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.message.TradingDataMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SuppressWarnings("NullableProblems")
@SideOnly(Side.CLIENT)
public class GuiVillager<T extends EntityLivingBase & IMerchant> extends GuiContainer {

    private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation(GamblingStyle.MODID, "textures/gui/container/merchant.png");

    private final T merchant;
    private final ITextComponent windowTitle;
    private final ITradingInfo.FilterMode filterMode;
    private int selectedMerchantRecipe;

    private final GuiTradingBook tradingBookGui = new GuiTradingBook();
    private final GhostTrade ghostTrade = new GhostTrade();

    public GuiVillager(InventoryPlayer playerInventory, T merchant, ITextComponent windowTitle, ITradingInfo.FilterMode filterMode) {

        super(new ContainerVillager(playerInventory, merchant, merchant.world));
        this.merchant = merchant;
        this.windowTitle = windowTitle;
        this.filterMode = filterMode;
        // TODO change this
        this.sendSelectedRecipe(false);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui() {

        super.initGui();
        // trading book might be open or not
        this.guiLeft = (this.width - this.xSize) / 2 + 57;
        this.tradingBookGui.initGui(this.mc, this.width, this.height);
        // not that this should every change, but it's updated in the super method
        this.ghostTrade.initGui(this.mc);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed() {

        this.tradingBookGui.removed();
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeByte(this.selectedMerchantRecipe);
        packetbuffer.writeInt(this.merchant.getEntityId());
        NetworkHandler.get().sendToServer(new TradingDataMessage(1, packetbuffer));
        super.onGuiClosed();
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        String s = this.windowTitle.getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2 + 23, 6, 4210752);
        this.fontRenderer.drawString(new TextComponentTranslation("container.inventory").getUnformattedText(), 62, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {

        super.updateScreen();
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            this.tradingBookGui.update(merchantrecipelist, (ContainerVillager) this.inventorySlots);
        }

        Slot hoveredSlot = this.getSlotUnderMouse();
        this.tradingBookGui.hoveredSlot = hoveredSlot != null ? hoveredSlot.getHasStack() ? 2 : 1 : 0;
        if (((ContainerVillager) this.inventorySlots).haveTradingSlotsContents()) {

            this.ghostTrade.clear();
        }
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop) {

        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + this.xSize || mouseY >= guiTop + this.ySize;

        return this.tradingBookGui.hasClickedOutside(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
        int guiLeft = this.guiLeft;
        int guiTop = this.guiTop;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            int selectedRecipe = this.selectedMerchantRecipe;
            MerchantRecipe merchantrecipe = merchantrecipelist.get(selectedRecipe);
            if (merchantrecipe.isRecipeDisabled()) {

                this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 32, 212, 0, 28, 21);
            }
        }

        GuiInventory.drawEntityOnScreen(guiLeft + 33, guiTop + 75, 30, guiLeft + 33 - mouseX,
                guiTop + 75 - 50 - mouseY, this.merchant);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        int recipeIndex = this.tradingBookGui.mouseClicked(mouseX, mouseY, mouseButton);
        switch (recipeIndex) {

            case -2:

                return;
            case -1:

                super.mouseClicked(mouseX, mouseY, mouseButton);
                break;
            default:

                MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
                if (merchantrecipelist != null) {

                    MerchantRecipe recipe = merchantrecipelist.get(recipeIndex);
                    boolean isNotSelected = this.selectedMerchantRecipe != recipeIndex;
                    boolean hasIngredients = this.tradingBookGui.hasRecipeContents(recipeIndex);
                    boolean isDisabled = recipe.isRecipeDisabled();
                    if (isNotSelected) {

                        this.selectedMerchantRecipe = recipeIndex;
                        this.sendSelectedRecipe(!hasIngredients || isDisabled);
                    }

                    if (hasIngredients) {

                        this.ghostTrade.clear();
                        if (!isDisabled) {

                            this.moveRecipeIngredients(isNotSelected, GuiScreen.isShiftKeyDown(), mouseButton == 1);
                        }
                    } else {

                        this.ghostTrade.setRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell());
                        if (((ContainerVillager) this.inventorySlots).haveTradingSlotsContents()) {

                            this.sendSelectedRecipe(true);
                        }
                    }
                }

        }
    }

    private void sendSelectedRecipe(boolean clear) {

        ((ContainerVillager) this.inventorySlots).setCurrentRecipeIndex(this.selectedMerchantRecipe);
        if (clear) {

            ((ContainerVillager) this.inventorySlots).clearTradingSlots();
        }

        this.tradingBookGui.setSelectedTradingRecipe(this.selectedMerchantRecipe);
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeByte(this.selectedMerchantRecipe);
        packetbuffer.writeBoolean(clear);
        NetworkHandler.get().sendToServer(new TradingDataMessage(0, packetbuffer));
    }

    private void moveRecipeIngredients(boolean clear, boolean quickMove, boolean skipMove) {
        ((ContainerVillager) this.inventorySlots).handleClickedButtonItems(this.selectedMerchantRecipe, clear, quickMove, skipMove);
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeByte(this.selectedMerchantRecipe);
        packetbuffer.writeBoolean(clear);
        packetbuffer.writeBoolean(quickMove);
        packetbuffer.writeBoolean(skipMove);
        NetworkHandler.get().sendToServer(new TradingDataMessage(2, packetbuffer));
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {

        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.tradingBookGui.countContents((ContainerVillager) this.inventorySlots);
        if (slotIn != null && slotId <= 2) {

            MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
            if (merchantrecipelist != null && merchantrecipelist.size() > this.selectedMerchantRecipe) {

                if (!merchantrecipelist.get(this.selectedMerchantRecipe).hasSecondItemToBuy() && slotId == 1) {

                    return;
                }

                this.ghostTrade.clear();
            }
        }
    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state) {

        this.tradingBookGui.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {

        this.tradingBookGui.handleMouseInput();
        super.handleMouseInput();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.drawDefaultBackground();
        this.tradingBookGui.render(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.ghostTrade.render(this.guiLeft, this.guiTop);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);
        if (merchantrecipelist != null) {

            MerchantRecipe merchantrecipe = merchantrecipelist.get(this.selectedMerchantRecipe);
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

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (!this.tradingBookGui.keyPressed(typedChar, keyCode)) {

            super.keyTyped(typedChar, keyCode);
        }
    }

    public IMerchant getMerchant() {

        return this.merchant;
    }

}