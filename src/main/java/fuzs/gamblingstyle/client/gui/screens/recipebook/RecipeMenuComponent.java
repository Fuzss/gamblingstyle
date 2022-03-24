package fuzs.gamblingstyle.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeMenuComponent extends GuiComponent implements Widget, GuiEventListener {
    private int leftPos, topPos;
    private RecipeBookMenu<?> menu;
    private Screen screen;
    protected ItemRenderer itemRenderer;
    public final NonNullList<RecipeSlot> slots = NonNullList.create();
    @Nullable
    private RecipeSlot hoveredSlot;
    @Nullable
    private RecipeSlot lastClickedSlot;

    public void addSlot(RecipeSlot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
    }

    public void setRecipe(int index, Recipe<?> recipe, boolean craftable) {
        this.slots.get(index).setRecipe(recipe, craftable);
    }

    public void init(RecipeBookMenu<?> menu, Screen screen, int leftPos, int topPos) {
        this.menu = menu;
        this.screen = screen;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.slots.clear();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        for (RecipeSlot slot : this.slots) {
            this.renderSlot(slot);
            if (this.isHovering(slot, mouseX, mouseY)) {
                this.hoveredSlot = slot;
                AbstractContainerScreen.renderSlotHighlight(poseStack, this.leftPos + slot.x, this.topPos + slot.y, this.getBlitOffset(), -1);
            }
        }
    }

    public void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasRecipe()) {
            List<Component> list = Lists.newArrayList(this.screen.getTooltipFromItem(this.hoveredSlot.getItem()));
            this.screen.renderComponentTooltip(poseStack, list, mouseX, mouseY, this.hoveredSlot.getItem());
        }
    }

    private void renderSlot(RecipeSlot slot) {
        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        RenderSystem.enableDepthTest();
        this.itemRenderer.renderAndDecorateFakeItem(slot.getItem(), this.leftPos + slot.x, this.topPos + slot.y);
        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    @Nullable
    private RecipeSlot findSlot(double mouseX, double mouseY) {
        for (RecipeSlot slot : this.slots) {
            if (this.isHovering(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isHovering(RecipeSlot p_97775_, double p_97776_, double p_97777_) {
        return this.isHovering(p_97775_.x, p_97775_.y, 16, 16, p_97776_, p_97777_);
    }

    protected boolean isHovering(int p_97768_, int p_97769_, int p_97770_, int p_97771_, double p_97772_, double p_97773_) {
        int i = this.leftPos;
        int j = this.topPos;
        p_97772_ -= i;
        p_97773_ -= j;
        return p_97772_ >= (double)(p_97768_ - 1) && p_97772_ < (double)(p_97768_ + p_97770_ + 1) && p_97773_ >= (double)(p_97769_ - 1) && p_97773_ < (double)(p_97769_ + p_97771_ + 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            RecipeSlot slot = this.findSlot(mouseX, mouseY);
            if (slot != null) {
                this.lastClickedSlot = slot;
                return true;
            }
        }
        return false;
    }

    @Nullable
    public RecipeSlot getLastClickedSlot() {
        return this.lastClickedSlot;
    }
}
