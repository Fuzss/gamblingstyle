package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.client.gui.GuiVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class SOpenWindowMessage extends Message<SOpenWindowMessage> {

    private int windowId;
    private ITextComponent windowTitle;
    private int slotCount;
    private int merchantId;
    private int lastTradeIndex;
    private ITradingInfo.FilterMode filterMode;
    private byte[] favoriteTrades;

    @SuppressWarnings("unused")
    public SOpenWindowMessage() {

    }

    public SOpenWindowMessage(int windowId, ITextComponent windowTitle, int slotCount, int merchantId, int lastTradeIndex, ITradingInfo.FilterMode filterMode, byte[] favoriteTrades) {

        this.windowId = windowId;
        this.windowTitle = windowTitle;
        this.slotCount = slotCount;
        this.merchantId = merchantId;
        this.lastTradeIndex = lastTradeIndex;
        this.filterMode = filterMode;
        this.favoriteTrades = favoriteTrades;
    }

    @Override
    public void write(ByteBuf buf) {

        buf.writeByte(this.windowId);
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(this.windowTitle));
        buf.writeByte(this.slotCount);
        buf.writeInt(this.merchantId);
        buf.writeByte(this.lastTradeIndex);
        buf.writeByte(this.filterMode.ordinal());
        buf.writeByte(this.favoriteTrades.length);
        for (byte favorite : this.favoriteTrades) {

            buf.writeByte(favorite);
        }
    }

    @Override
    public void read(ByteBuf buf) {

        this.windowId = buf.readUnsignedByte();
        this.windowTitle = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        this.slotCount = buf.readUnsignedByte();
        this.merchantId = buf.readInt();
        this.lastTradeIndex = buf.readUnsignedByte();
        this.filterMode = ITradingInfo.FilterMode.values()[buf.readUnsignedByte()];
        int tradesLength = buf.readUnsignedByte();
        this.favoriteTrades = new byte[tradesLength];
        for (int i = 0; i < tradesLength; i++) {

            this.favoriteTrades[i] = (byte) buf.readUnsignedByte();
        }
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new OpenWindowProcessor<>();
    }

    private class OpenWindowProcessor<T extends EntityLivingBase & IMerchant> implements MessageProcessor {

        @SuppressWarnings("unchecked")
        @Override
        public void accept(EntityPlayer player) {

            World worldIn = player.world;
            Entity entity = worldIn.getEntityByID(SOpenWindowMessage.this.merchantId);
            if (entity instanceof EntityLivingBase && entity instanceof IMerchant) {

                T merchant = (T) entity;
                merchant.setCustomer(player);
                GuiVillager<T> guiContainer = new GuiVillager<>(player.inventory, merchant, SOpenWindowMessage.this.windowTitle, SOpenWindowMessage.this.lastTradeIndex, SOpenWindowMessage.this.filterMode, SOpenWindowMessage.this.favoriteTrades);
                Minecraft.getMinecraft().displayGuiScreen(guiContainer);
                player.openContainer.windowId = SOpenWindowMessage.this.windowId;
            }
        }

    }
}