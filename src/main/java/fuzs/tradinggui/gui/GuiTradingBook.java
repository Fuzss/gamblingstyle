package fuzs.tradinggui.gui;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.IMerchant;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.Iterator;
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

    private List<GuiButtonTradingRecipe> buttons = Lists.<GuiButtonTradingRecipe>newArrayListWithCapacity(20);
    private boolean isOpen;
    private int xPos;
    private int yPos;

    public void initGui(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.width = width;
        this.height = height;
        this.xPos = (this.width - xSize) / 2 - this.xOffset;
        this.yPos = (this.height - ySize) / 2;

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
            this.buttons.get(i).init(activemerchantrecipe.getItemToBuy(), activemerchantrecipe.hasSecondItemToBuy() ? activemerchantrecipe.getSecondItemToBuy() : null, activemerchantrecipe.getItemToSell(), activemerchantrecipe.isRecipeDisabled());
            this.buttons.get(i).setPosition(this.xPos + 8, this.yPos + 21 + 25 * i);
        }
    }

    public boolean isVisible()
    {
        return this.isOpen;
    }

    public void setVisible(boolean p_193006_1_)
    {
        this.isOpen = p_193006_1_;
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        if(this.isVisible()) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 100.0F);
            this.mc.getTextureManager().bindTexture(RECIPE_BOOK);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPos, this.yPos, 0, 0, this.xSize, this.ySize);

            for (GuiButtonTradingRecipe guiButtonTradingRecipe : this.buttons) {
                if (guiButtonTradingRecipe.hasRecipe()) {
                    guiButtonTradingRecipe.drawButton(this.mc, mouseX, mouseY, partialTicks);
                } else {
                    break;
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }
}