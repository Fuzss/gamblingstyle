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
        SELLS("gui.button.show.sells");

        private final String translationKey;

        FilterMode(String translationKey) {

            this.translationKey = translationKey;
        }

        public String getTranslationKey() {

            return this.translationKey;
        }

        public boolean isSells() {

            return this != BUYS;
        }

        public boolean isBuys() {

            return this != SELLS;
        }

    }

}
