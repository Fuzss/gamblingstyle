package fuzs.tradinggui.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class GuiTradingBook extends Gui
{
    protected static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/container/merchant_book.png");
    private final int xOffset = 88;
    private int width;
    private int height;
    private final int xSize = 112;
    private final int ySize = 166;
    private Minecraft mc;

    private GuiButtonTradingRecipe hoveredButton;
    private List<GuiButtonTradingRecipe> buttonList = Lists.newArrayListWithCapacity(4);
    private GuiTextField searchField;
    private String lastSearch = "";
    private int guiLeft;
    private int guiTop;
    private boolean sentRecipeList;
    private boolean populate;
    private TradingRecipeList tradingRecipeList;
    /** The button that was just pressed. */
    protected GuiButton selectedButton;
    private int selectedTradingRecipe;

    public void initGui(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.width = width;
        this.height = height;
        this.guiLeft = (this.width - xSize) / 2 - this.xOffset;
        this.guiTop = (this.height - ySize) / 2;

        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        this.searchField = new GuiTextField(0, mc.fontRenderer, this.guiLeft + 9, this.guiTop + 9,
                80, mc.fontRenderer.FONT_HEIGHT);
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);

        this.sentRecipeList = false;
        this.populate = false;
        this.selectedTradingRecipe = 0;

        for (int i = 0; i <= 4; ++i)
        {
            this.buttonList.add(new GuiButtonTradingRecipe(i, this.guiLeft + 8, this.guiTop + 21 + 25 * i));
            this.buttonList.get(i).visible = false;
        }

    }

    public void removed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    public void setSelectedTradingRecipe(int i) {

        if (this.tradingRecipeList != null) {
            this.tradingRecipeList.get(this.selectedTradingRecipe).setIsSelected(false);
            this.selectedTradingRecipe = i;
            this.tradingRecipeList.get(this.selectedTradingRecipe).setIsSelected(true);
            this.populate = true;
        } else {
            this.selectedTradingRecipe = i;
        }

    }

    public void update(MerchantRecipeList merchantrecipelist)
    {
        if (!this.sentRecipeList) {
            this.tradingRecipeList = new TradingRecipeList(merchantrecipelist);
            this.tradingRecipeList.get(this.selectedTradingRecipe).setIsSelected(true);
            this.sentRecipeList = true;
            this.populate = true;
        }

        if (this.tradingRecipeList != null && this.populate) {

            if (this.tradingRecipeList.size() != merchantrecipelist.size()) {
                return;
            }

            int i = 0;

            for (GuiButtonTradingRecipe guiButtonTradingRecipe : this.buttonList) {

                guiButtonTradingRecipe.visible = false;

                for (int j = i; j < this.tradingRecipeList.size(); j++) {

                    TradingRecipe tradingRecipe = this.tradingRecipeList.get(j);

                    if (tradingRecipe.isValidRecipe() && tradingRecipe.getIsSearchResult()) {
                        guiButtonTradingRecipe.setContents(j, tradingRecipe.getItemToBuy(),
                                tradingRecipe.getSecondItemToBuy(), tradingRecipe.getItemToSell(), merchantrecipelist.get(j).isRecipeDisabled(), tradingRecipe.getIsSelected());
                        i = j + 1;
                        guiButtonTradingRecipe.visible = true;
                        break;
                    } else {
                        i++;
                    }
                }

            }
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 100.0F);
        this.mc.getTextureManager().bindTexture(RECIPE_BOOK);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();

        this.hoveredButton = null;

        for (GuiButtonTradingRecipe guiButtonTradingRecipe : this.buttonList) {

            guiButtonTradingRecipe.drawButton(this.mc, mouseX, mouseY, partialTicks);

            if (guiButtonTradingRecipe.isMouseOver() && guiButtonTradingRecipe.visible)
            {
                this.hoveredButton = guiButtonTradingRecipe;
            }

        }

        GlStateManager.popMatrix();
    }

    public void renderTooltip(int mouseX, int mouseY)
    {
        if (mc.currentScreen != null && this.hoveredButton != null)
        {
            List<String> tooltip = this.hoveredButton.getToolTipText(mc.currentScreen, mouseX, mouseY);
            if (tooltip != null && mc.player.inventory.getItemStack().isEmpty()) {
                mc.currentScreen.drawHoveringText(tooltip, mouseX, mouseY);
            }
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected int mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (this.searchField.mouseClicked(mouseX, mouseY, mouseButton)) {
            return -2;
        }

        if (mouseButton == 0) {
            for (GuiButtonTradingRecipe guiButtonTradingRecipe : this.buttonList) {

                if (guiButtonTradingRecipe.mousePressed(this.mc, mouseX, mouseY)) {
                    this.selectedButton = guiButtonTradingRecipe;
                    guiButtonTradingRecipe.playPressSound(this.mc.getSoundHandler());
                    return guiButtonTradingRecipe.getRecipeId();
                }
            }
        }

        return -1;

    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (this.selectedButton != null && state == 0)
        {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    public boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize)
    {
        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + xSize || mouseY >= guiTop + ySize;
        boolean flag1 = guiLeft - this.xSize < mouseX && mouseX < guiLeft && guiTop < mouseY && mouseY < guiTop + ySize;
        return flag && !flag1;
    }

    public boolean keyPressed(char typedChar, int keycode)
    {
        if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat) && !this.searchField.isFocused())
        {
            this.searchField.setFocused(true);
        }
        else if (this.searchField.textboxKeyTyped(typedChar, keycode))
        {
            String s1 = this.searchField.getText().toLowerCase(Locale.ROOT);

            if (!s1.equals(this.lastSearch) && this.tradingRecipeList != null)
            {
                this.tradingRecipeList.searchQuery(s1, this.mc);
                this.lastSearch = s1;
                this.populate = true;
            }

            return true;
        }

        return false;
    }
}