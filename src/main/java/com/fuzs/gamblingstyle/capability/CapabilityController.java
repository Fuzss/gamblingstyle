package com.fuzs.gamblingstyle.capability;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.capability.container.TradingInfo;
import com.fuzs.gamblingstyle.capability.core.CapabilityDispatcher;
import com.fuzs.gamblingstyle.capability.core.CapabilityStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityController {

    @CapabilityInject(ITradingInfo.class)
    public static final Capability<TradingInfo> TRADING_INFO_CAPABILITY = null;

    public CapabilityController() {

        CapabilityManager.INSTANCE.register(ITradingInfo.class, new CapabilityStorage<>(), TradingInfo::new);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onAttachCapabilities(final AttachCapabilitiesEvent<Entity> evt) {

        if (evt.getObject() instanceof EntityLivingBase && evt.getObject() instanceof IMerchant) {

            evt.addCapability(new ResourceLocation(GamblingStyle.MODID, "trading_info"), new CapabilityDispatcher<>(new TradingInfo(), TRADING_INFO_CAPABILITY));
        }
    }

    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap) {

        return provider.getCapability(cap, null);
    }

}
