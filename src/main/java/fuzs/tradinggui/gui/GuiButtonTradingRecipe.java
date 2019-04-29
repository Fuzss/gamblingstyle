package fuzs.tradinggui.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiButtonTradingRecipe extends GuiButton
{
    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/container/merchant_book.png");

    private ItemStack input1 = ItemStack.EMPTY;
    private ItemStack input2 = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private boolean soldOut;

    public GuiButtonTradingRecipe()
    {
        super(0, 0, 0, 88, 25, "");
    }

    public void setContents(ItemStack itemStack, ItemStack itemStack1, ItemStack itemStack2, boolean soldOut)
    {
        this.input1 = itemStack;
        this.input2 = itemStack1;
        this.output = itemStack2;
        this.soldOut = soldOut;
    }

    public boolean hasRecipe()
    {
        return !this.output.isEmpty() && !this.input1.isEmpty();
    }

    public void setPosition(int posX, int posY)
    {
        this.x = posX;
        this.y = posY;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            RenderHelper.enableGUIStandardItemLighting();
            mc.getTextureManager().bindTexture(RECIPE_BOOK);
            GlStateManager.disableLighting();
            int i = 112;
            int j = 0;

            this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);

            mc.getRenderItem().renderItemAndEffectIntoGUI(this.input1, this.x + 6, this.y + 4);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.input1, this.x + 6, this.y + 4);
            if (!this.input2.isEmpty()) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(this.input2, this.x + 31, this.y + 4);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.input2, this.x + 31, this.y + 4);
            }
            mc.getRenderItem().renderItemAndEffectIntoGUI(this.output, this.x + 65, this.y + 4);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.output, this.x + 65, this.y + 4);

            if (soldOut) {
                mc.getTextureManager().bindTexture(RECIPE_BOOK);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.x + 51, this.y + 5, 112, 50, 10, 15);
            }

            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
        }
    }

    public int getButtonWidth()
    {
        return 25;
    }

    /**
     * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
     * pointY
     */
    private boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        int i = this.x;
        int j = this.y;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    public List<String> getToolTipText(GuiScreen screen, int mouseX, int mouseY)
    {
        ItemStack itemstack = this.getItemStackInRegion(mouseX, mouseY);
        List<String> list = Lists.<String>newArrayList();

        if (!itemstack.isEmpty()) {
            list = screen.getItemToolTip(itemstack);
        } else if (this.soldOut && this.isPointInRegion(51, 5, 10, 15, mouseX, mouseY))
        {
            list.add(I18n.format("merchant.deprecated"));
        }

        return list;
    }

    private ItemStack getItemStackInRegion(int mouseX, int mouseY) {

        if (this.isPointInRegion(6, 4, 16, 16, mouseX, mouseY) && !this.input1.isEmpty())
        {
            return this.input1;
        }
        else if (this.isPointInRegion(31, 4, 16, 16, mouseX, mouseY) && !this.input2.isEmpty())
        {
            return this.input2;
        }
        else if (this.isPointInRegion(65, 4, 16, 16, mouseX, mouseY) && !this.output.isEmpty())
        {
            return this.output;
        }
        else {
            return ItemStack.EMPTY;
        }

    }
}