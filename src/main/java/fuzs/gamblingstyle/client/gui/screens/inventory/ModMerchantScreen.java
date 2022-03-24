package fuzs.gamblingstyle.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.gamblingstyle.GamblingStyle;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ModMerchantScreen extends AbstractContainerScreen<MerchantMenu> {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation(GamblingStyle.MOD_ID, "textures/gui/container/merchant.png");

    private final Slot[] tradingSlots;
    private int activeTradeOffer;

    public ModMerchantScreen(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 304;
        this.inventoryLabelX = 135;
        Slot[] tradingSlots = new Slot[3];
        for (int i = 0; i < 3; i++) {
            tradingSlots[i] = this.menu.getSlot(i);
        }
        this.tradingSlots = tradingSlots;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 91 + (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
        int posX = (this.width - this.imageWidth) / 2;
        int posY = (this.height - this.imageHeight) / 2;
        blit(poseStack, posX, posY, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
        if (this.isActiveTradeOutOfStock()) {
            RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            blit(poseStack, this.leftPos + 225, this.topPos + 32, this.getBlitOffset(), 340, 0, 28, 21, 512, 256);
        }
    }

    private boolean isActiveTradeOutOfStock() {
        MerchantOffers merchantoffers = this.menu.getOffers();
        if (!merchantoffers.isEmpty()) {
            if (this.activeTradeOffer >= 0 && this.activeTradeOffer < merchantoffers.size()) {
                MerchantOffer merchantoffer = merchantoffers.get(this.activeTradeOffer);
                return merchantoffer.isOutOfStock();
            }
        }
        return false;
    }

//    private class ScrollingList extends AbstractContainerEventHandler implements Widget, NarratableEntry {
//        private final List<EnchantmentListEntry> children = Lists.newArrayList();
//        private final int posX;
//        private final int posY;
//        private final int itemWidth;
//        private final int itemHeight;
//        private final int length;
//        private int scrollPosition;
//
//        public ScrollingList(int posX, int posY, int itemWidth, int itemHeight, int length) {
//            this.posX = posX;
//            this.posY = posY;
//            this.itemWidth = itemWidth;
//            this.itemHeight = itemHeight;
//            this.length = length;
//        }
//
//        public void scrollTo(float pos) {
//            if (pos < 0.0F || pos > 1.0F) throw new IllegalArgumentException("pos must be of interval 0 to 1");
//            if (this.canScroll()) {
//                // important to round instead of int cast
//                this.scrollPosition = Math.round((this.getItemCount() - this.length) * pos);
//            } else {
//                this.scrollPosition = 0;
//            }
//        }
//
//        public boolean canScroll() {
//            return this.getItemCount() > this.length;
//        }
//
//        protected final void clearEntries() {
//            this.children.clear();
//        }
//
//        protected void addEntry(EnchantmentListEntry pEntry) {
//            this.children.add(pEntry);
//            pEntry.setList(this);
//            this.markOthersIncompatible();
//        }
//
//        protected int getItemCount() {
//            return this.children.size();
//        }
//
//        public void markOthersIncompatible() {
//            final List<EnchantmentListEntry> activeEnchants = this.children.stream()
//                    .filter(EnchantmentListEntry::isActive)
//                    .toList();
//            for (EnchantmentListEntry entry : this.children) {
//                if (!entry.isActive()) {
//                    entry.markIncompatible(activeEnchants.stream()
//                            .filter(e -> e.isIncompatibleWith(entry))
//                            .collect(Collectors.toSet()));
//                }
//            }
//        }
//
//        @Nullable
//        protected final EnchantmentListEntry getEntryAtPosition(double mouseX, double mouseY) {
//            if (this.isMouseOver(mouseX, mouseY)) {
//                final int index = this.scrollPosition + (int) ((mouseY - this.posY) / this.itemHeight);
//                return index < this.children.size() ? this.children.get(index) : null;
//            }
//            return null;
//        }
//
//        @Override
//        public boolean isMouseOver(double mouseX, double mouseY) {
//            return mouseX >= this.posX && mouseX < this.posX + this.itemWidth && mouseY >= this.posY && mouseY < this.posY + this.itemHeight * this.length;
//        }
//
//        @Override
//        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//            if (!this.isMouseOver(mouseX, mouseY)) {
//                return false;
//            } else {
//                EnchantmentListEntry entry = this.getEntryAtPosition(mouseX, mouseY);
//                if (entry != null) {
//                    if (entry.mouseClicked(mouseX, mouseY, button)) {
//                        this.setFocused(entry);
//                        this.setDragging(true);
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//
//        @Override
//        public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
//            if (this.getFocused() != null) {
//                this.getFocused().mouseReleased(pMouseX, pMouseY, pButton);
//            }
//            return false;
//        }
//
//        @Override
//        public List<EnchantmentListEntry> children() {
//            return this.children;
//        }
//
//        @Override
//        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
//            for (int i = 0; i < Math.min(this.length, this.getItemCount()); i++) {
//                this.children.get(this.scrollPosition + i).render(poseStack, this.posX, this.posY + this.itemHeight * i, this.itemWidth, this.itemHeight, mouseX, mouseY, partialTick);
//            }
//        }
//
//        @Override
//        public NarrationPriority narrationPriority() {
//            // TODO proper implementation
//            return NarratableEntry.NarrationPriority.NONE;
//        }
//
//        @Override
//        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
//            // TODO proper implementation
//        }
//    }
//
//    private class TradeOfferEntry implements ContainerEventHandler {
//
//    }
}
