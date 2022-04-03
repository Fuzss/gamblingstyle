package fuzs.gamblingstyle.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TabIconButton extends IconButton {
    private final int originalXTexStart;
    private final Font font;
    private final ItemRenderer itemRenderer;
    @Nullable
    private CreativeModeTab tab;

    public TabIconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, xTexStart, yTexStart, resourceLocation, textureWidth, textureHeight, onPress, onTooltip, TextComponent.EMPTY);
        this.originalXTexStart = xTexStart;
        this.visible = false;
        Minecraft minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
    }

    public void setCreativeTab(@Nullable CreativeModeTab tab) {
        this.tab = tab;
        this.visible = tab != null;
    }

    public CreativeModeTab getCreativeTab() {
        return this.tab;
    }

    public void markCreativeTab(boolean active) {
        if (active) {
            this.xTexStart = this.originalXTexStart + this.width;
        } else {
            this.xTexStart = this.originalXTexStart;
        }
    }

    @Override
    protected int getYImage(boolean p_93668_) {
        return 0;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        this.itemRenderer.blitOffset = 100.0F;
        ItemStack item = this.tab.getIconItem();
        this.itemRenderer.renderAndDecorateItem(item, this.x + 3, this.y + 3);
        this.itemRenderer.renderGuiItemDecorations(this.font, item, this.x + 3, this.y + 3);
        this.itemRenderer.blitOffset = 0.0F;
    }

    @Override
    protected void renderButtonBackground(PoseStack poseStack, int mouseX, int mouseY) {

    }
}
