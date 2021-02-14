package com.fuzs.gamblingstyle.util;

import com.fuzs.gamblingstyle.GamblingStyle;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface IPrivateAccessor {

    String[] ENTITYVILLAGER_WEALTH = new String[]{"wealth", "field_70956_bz"};

    default void setWealth(EntityVillager instance, int wealth) {
        try {
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, instance, wealth, ENTITYVILLAGER_WEALTH[1]);
        } catch (Exception ex) {
            GamblingStyle.LOGGER.error("setWealth() failed", ex);
        }
    }

    default int getWealth(EntityVillager instance) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, instance, ENTITYVILLAGER_WEALTH[1]);
        } catch (Exception ex) {
            GamblingStyle.LOGGER.error("getWealth() failed", ex);
        }
        return 0;
    }
}
