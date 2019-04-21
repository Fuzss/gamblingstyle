package fuzs.tradinggui.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

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
    private List<GuiButtonTradingRecipe> buttons = Lists.<GuiButtonTradingRecipe>newArrayListWithCapacity(20);
    private int guiLeft;
    private int guiTop;

    public void initGui(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.width = width;
        this.height = height;
        this.guiLeft = (this.width - xSize) / 2 - this.xOffset;
        this.guiTop = (this.height - ySize) / 2;

        for (int i = 0; i < 20; ++i)
        {
            this.buttons.add(new GuiButtonTradingRecipe());
        }

    }

    public void update(MerchantRecipeList merchantrecipelist)
    {
        for (int i = 0; i < merchantrecipelist.size(); ++i)
        {
            MerchantRecipe activemerchantrecipe = merchantrecipelist.get(i);
            this.buttons.get(i).init(activemerchantrecipe.getItemToBuy(), activemerchantrecipe.hasSecondItemToBuy() ? activemerchantrecipe.getSecondItemToBuy() : ItemStack.EMPTY, activemerchantrecipe.getItemToSell(), activemerchantrecipe.isRecipeDisabled());
            this.buttons.get(i).setPosition(this.guiLeft + 8, this.guiTop + 21 + 25 * i);
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

        this.hoveredButton = null;
        for (GuiButtonTradingRecipe guiButtonTradingRecipe : this.buttons) {
            if (guiButtonTradingRecipe.hasRecipe()) {
                guiButtonTradingRecipe.drawButton(this.mc, mouseX, mouseY, partialTicks);

                if (guiButtonTradingRecipe.visible && guiButtonTradingRecipe.isMouseOver())
                {
                    this.hoveredButton = guiButtonTradingRecipe;
                }
            } else {
                break;
            }
        }

        RenderHelper.disableStandardItemLighting();
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

    public boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize)
    {
        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + xSize || mouseY >= guiTop + ySize;
        boolean flag1 = guiLeft - this.xSize < mouseX && mouseX < guiLeft && guiTop < mouseY && mouseY < guiTop + ySize;
        return flag && !flag1;
    }
}