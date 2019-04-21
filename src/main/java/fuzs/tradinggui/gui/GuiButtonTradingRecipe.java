package fuzs.tradinggui.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiButtonTradingRecipe extends GuiButton
{
    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/container/merchant_book.png");

    private ItemStack input1;
    private ItemStack input2;
    private ItemStack output;
    private boolean soldOut;

    public GuiButtonTradingRecipe()
    {
        super(0, 0, 0, 88, 25, "");
    }

    public void init(ItemStack itemStack, @Nullable ItemStack itemStack1, ItemStack itemStack2, boolean soldOut)
    {
        this.input1 = itemStack;
        this.input2 = itemStack1;
        this.output = itemStack2;
        this.soldOut = soldOut;
    }

    public boolean hasRecipe()
    {
        return this.output != null;
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
            RenderHelper.enableGUIStandardItemLighting();
            mc.getTextureManager().bindTexture(RECIPE_BOOK);
            GlStateManager.disableLighting();
            int i = 112;
            int j = 0;

            this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);

            mc.getRenderItem().renderItemAndEffectIntoGUI(this.input1, this.x + 7, this.y + 4);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.input1, this.x + 7, this.y + 4);
            mc.getRenderItem().renderItemAndEffectIntoGUI(this.output, this.x + 67, this.y + 4);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.output, this.x + 67, this.y + 4);
            if (this.input2 != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(this.input2, this.x + 34, this.y + 4);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, this.input2, this.x + 34, this.y + 4);
            }

            if (soldOut) {
                mc.getTextureManager().bindTexture(RECIPE_BOOK);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.x + 52, this.y + 6, 112, 50, 15, 15);
            }

            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
        }
    }

    public int getButtonWidth()
    {
        return 25;
    }
}