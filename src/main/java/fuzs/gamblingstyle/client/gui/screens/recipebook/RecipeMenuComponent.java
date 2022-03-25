package fuzs.gamblingstyle.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.gamblingstyle.GamblingStyle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeMenuComponent extends GuiComponent implements Widget, GuiEventListener {
    private int leftPos, topPos;
    private Minecraft minecraft;
    public final NonNullList<RecipeSlot> slots = NonNullList.create();
    @Nullable
    private RecipeSlot hoveredSlot;
    // using hoveredSlot would probably work, but maybe with some lag/dropped frames or whatever they could be different?
    @Nullable
    private RecipeSlot lastClickedSlot;

    public void addSlot(RecipeSlot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
    }

    public void setRecipe(int index, @Nullable Recipe<?> recipe, boolean craftable, boolean favorite) {
        this.slots.get(index).setRecipe(recipe, craftable, favorite);
    }

    public void clearRecipe(int index) {
        this.slots.get(index).setRecipe(null, true, false);
    }

    public void init(Minecraft minecraft, int leftPos, int topPos) {
        this.slots.clear();
        this.minecraft = minecraft;
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        for (RecipeSlot slot : this.slots) {
            this.renderSlot(poseStack, slot);
            if (this.isHovering(slot, mouseX, mouseY)) {
                this.hoveredSlot = slot;
                AbstractContainerScreen.renderSlotHighlight(poseStack, this.leftPos + slot.x, this.topPos + slot.y, this.getBlitOffset());
            }
        }
    }

    public void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, Screen screen) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasRecipe()) {
            List<Component> list = Lists.newArrayList(screen.getTooltipFromItem(this.hoveredSlot.getItem()));
            screen.renderComponentTooltip(poseStack, list, mouseX, mouseY, this.hoveredSlot.getItem());
        }
    }

    private void renderSlot(PoseStack poseStack, RecipeSlot slot) {
        poseStack.pushPose();
        int posX = this.leftPos + slot.x;
        int posY = this.topPos + slot.y;
        if (GamblingStyle.CONFIG.client().colorfulRecipeBackgrounds) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ModRecipeBookComponent.RECIPE_BOOK_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int texOff = 0;
            if (slot.hasRecipe()) {
                texOff += slot.hasCraftable() ? 18 : 36;
            }
            this.blit(poseStack, posX - 1, posY - 1, 147 + texOff, 126, 18, 18);
        }
        this.setBlitOffset(100);
        this.minecraft.getItemRenderer().blitOffset = 100.0F;
        RenderSystem.enableDepthTest();
        if (!GamblingStyle.CONFIG.client().colorfulRecipeBackgrounds) {
            if (!slot.hasCraftable()) {
                GuiComponent.fill(poseStack, posX, posY, posX + 16, posY + 16, 822018048);
            }
        }
        this.minecraft.getItemRenderer().renderAndDecorateFakeItem(slot.getItem(), posX, posY);
        if (!GamblingStyle.CONFIG.client().colorfulRecipeBackgrounds) {
            if (!slot.hasCraftable()) {
                RenderSystem.depthFunc(516);
                GuiComponent.fill(poseStack, posX, posY, posX + 16, posY + 16, 822083583);
                RenderSystem.depthFunc(515);
            }
        }
        if (slot.isFavorite()) {
            this.renderFavoriteIcon(poseStack, posX, posY);
        }
        this.minecraft.getItemRenderer().blitOffset = 0.0F;
        this.setBlitOffset(0);
        poseStack.popPose();
    }

    private void renderFavoriteIcon(PoseStack poseStack, int posX, int posY) {
        poseStack.pushPose();
        float timeOffset = (Util.getMillis() % 2000) / 1000.0F;
        timeOffset = this.easeInOutQuad(timeOffset);
        poseStack.translate(0, timeOffset - 0.5F, 0.0);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ModRecipeBookComponent.RECIPE_BOOK_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(poseStack, posX + 1, posY + 2, 149, 153, 6, 6);
        poseStack.popPose();
    }

    private float easeInOutQuad(float timeOffset) {
        timeOffset = Mth.clamp(timeOffset, 0.0F, 2.0F);
        if (timeOffset >= 1.0F) timeOffset = 2.0F - timeOffset;
        timeOffset = timeOffset < 0.5F ? 2.0F * timeOffset * timeOffset : 1.0F - (-2.0F * timeOffset + 2.0F) * (-2.0F * timeOffset + 2.0F) / 2.0F;
        return timeOffset;
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
        this.lastClickedSlot = null;
        RecipeSlot slot = this.findSlot(mouseX, mouseY);
        if (slot != null) {
            if (button == 0) {
                this.lastClickedSlot = slot;
            } else if (button == 1 && slot.hasRecipe()) {
                Recipe<?> recipe = slot.getRecipe();
                GamblingStyle.LOGGER.info("{} {} {}", recipe.getId(), recipe.getGroup(), Registry.RECIPE_TYPE.getKey(recipe.getType()));
            }
            return true;
        }
        return false;
    }

    @Nullable
    public RecipeSlot getLastClickedSlot() {
        return this.lastClickedSlot;
    }
}
