package com.fuzs.gamblingstyle.capability.container;

import net.minecraft.nbt.NBTTagCompound;

public class TradingInfo implements ITradingInfo {

    private int lastTradeIndex;
    private FilterMode filterMode;
    private byte[] favoriteTrades;

    @Override
    public int getLastTradeIndex() {

        return this.lastTradeIndex;
    }

    @Override
    public void setLastTradeIndex(int lastTradeIndex) {

        this.lastTradeIndex = lastTradeIndex;
    }

    @Override
    public FilterMode getFilterMode() {

        return this.filterMode;
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {

        this.filterMode = filterMode;
    }

    @Override
    public byte[] getFavoriteTrades() {

        return this.favoriteTrades;
    }

    @Override
    public void setFavoriteTrades(byte[] favoriteTrades) {

        this.favoriteTrades = favoriteTrades;
    }

    @Override
    public NBTTagCompound serializeNBT() {

        NBTTagCompound compound = new NBTTagCompound();
        compound.setByte("LastTradeIndex", (byte) this.lastTradeIndex);
        compound.setByte("FilterMode", (byte) this.filterMode.ordinal());
        compound.setByteArray("FavoriteTrades", this.favoriteTrades);

        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {

        this.lastTradeIndex = compound.getByte("LastTradeIndex");
        this.filterMode = FilterMode.values()[compound.getByte("FilterMode")];
        this.favoriteTrades = compound.getByteArray("FavoriteTrades");
    }

}
