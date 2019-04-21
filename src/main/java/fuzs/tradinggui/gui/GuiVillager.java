package fuzs.tradinggui.gui;

import fuzs.tradinggui.inventory.ContainerVillager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiVillager extends GuiContainer
{
    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;
    /** The GUI texture for the villager merchant GUI. */
    private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("textures/gui/container/merchant.png");
    /** The current IMerchant instance in use for this specific merchant. */
    private final IMerchant merchant;
    private final EntityVillager entityVillager;
    /** The integer value corresponding to the currently selected merchant recipe. */
    private int selectedMerchantRecipe;
    /** The chat component utilized by this GuiVillager instance. */
    private final ITextComponent chatComponent;

    private final GuiTradingBook tradingBookGui = new GuiTradingBook();

    public GuiVillager(InventoryPlayer p_i45500_1_, IMerchant p_i45500_2_, EntityVillager entityVillager, World worldIn)
    {
        super(new ContainerVillager(p_i45500_1_, p_i45500_2_, worldIn));
        this.merchant = p_i45500_2_;
        this.entityVillager = entityVillager;
        this.chatComponent = p_i45500_2_.getDisplayName();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2 + 57;
        this.tradingBookGui.initGui(this.mc, this.width, this.height);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = this.chatComponent.getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2 + 23, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 62, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);

        if (merchantrecipelist != null)
        {
            this.tradingBookGui.update(merchantrecipelist);
        }
    }

    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop)
    {
        boolean flag = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + this.xSize || mouseY >= guiTop + this.ySize;
        return this.tradingBookGui.hasClickedOutside(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        boolean flag = false;

        if (flag)
        {
            ((ContainerVillager)this.inventorySlots).setCurrentRecipeIndex(this.selectedMerchantRecipe);
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(this.selectedMerchantRecipe);
            this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|TrSel", packetbuffer));
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
        {
            int k = this.selectedMerchantRecipe;

            if (k < 0 || k >= merchantrecipelist.size())
            {
                return;
            }

            MerchantRecipe merchantrecipe = merchantrecipelist.get(k);

            if (merchantrecipe.isRecipeDisabled())
            {
                this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 32, 212, 0, 28, 21);
            }
        }

        GuiInventory.drawEntityOnScreen(i + 33, j + 75, 30, (float)(i + 33) - this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, (EntityVillager) this.entityVillager);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        this.tradingBookGui.render(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);

        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes(this.mc.player);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
        {

            int k = this.selectedMerchantRecipe;
            MerchantRecipe merchantrecipe = merchantrecipelist.get(k);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            if (merchantrecipe.isRecipeDisabled() && this.isPointInRegion(97, 32, 28, 21, mouseX, mouseY))
            {
                this.drawHoveringText(I18n.format("merchant.deprecated"), mouseX, mouseY);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.tradingBookGui.renderTooltip(mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    public IMerchant getMerchant()
    {
        return this.merchant;
    }
}