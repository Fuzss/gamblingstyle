package com.fuzs.gamblingstyle.capability.container;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ITradingInfo extends INBTSerializable<NBTTagCompound> {

    int getLastTradeIndex();

    void setLastTradeIndex(int lastTradeIndex);

    FilterMode getFilterMode();

    void setFilterMode(FilterMode filterMode);

    byte[] getFavoriteTrades();

    void setFavoriteTrades(byte[] favoriteTrades);

    enum FilterMode {

        ALL("gui.button.show.all"),
        BUYS("gui.button.show.buys"),
        SELLS("gui.button.show.sells"),
        FAVORITES("gui.button.show.favorites");

        public final String key;

        FilterMode(String key) {

            this.key = key;
        }

        public boolean isSells() {

            return this == SELLS || this == ALL;
        }

        public boolean isBuys() {

            return this == BUYS || this == ALL;
        }

    }

}
