package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.capability.CapabilityController;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CSyncTradingInfoMessage extends Message<CSyncTradingInfoMessage> {

    private int merchantId;
    private int lastTradeIndex;
    private ITradingInfo.FilterMode filterMode;
    private byte[] favoriteTrades;

    @SuppressWarnings("unused")
    public CSyncTradingInfoMessage() {

    }

    public CSyncTradingInfoMessage(int merchantId, int lastTradeIndex, ITradingInfo.FilterMode filterMode, byte[] favoriteTrades) {

        this.merchantId = merchantId;
        this.lastTradeIndex = lastTradeIndex;
        this.filterMode = filterMode;
        this.favoriteTrades = favoriteTrades;
    }

    @Override
    public void write(ByteBuf buf) {

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

        return new SyncTradingInfoProcessor();
    }

    private class SyncTradingInfoProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            World worldIn = player.world;
            Entity entity = worldIn.getEntityByID(CSyncTradingInfoMessage.this.merchantId);
            if (entity instanceof EntityLivingBase && entity instanceof IMerchant) {

                ITradingInfo tradingInfo = CapabilityController.getCapability(entity, CapabilityController.TRADING_INFO_CAPABILITY);
                tradingInfo.setLastTradeIndex(CSyncTradingInfoMessage.this.lastTradeIndex);
                tradingInfo.setFilterMode(CSyncTradingInfoMessage.this.filterMode);
                tradingInfo.setFavoriteTrades(CSyncTradingInfoMessage.this.favoriteTrades);
            }
        }

    }

}