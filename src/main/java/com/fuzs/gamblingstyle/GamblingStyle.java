package com.fuzs.gamblingstyle;

import com.fuzs.gamblingstyle.handler.ModEventHandler;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = GamblingStyle.MODID,
        name = GamblingStyle.NAME,
        version = GamblingStyle.VERSION,
        acceptedMinecraftVersions = GamblingStyle.RANGE,
        dependencies = GamblingStyle.DEPENDENCIES,
        certificateFingerprint = GamblingStyle.FINGERPRINT
)
public class GamblingStyle
{
    public static final String MODID = "gamblingstyle";
    public static final String NAME = "Gambling Style";
    public static final String VERSION = "@VERSION@";
    public static final String RANGE = "[1.12.2]";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,)";
    public static final String CLIENT_PROXY_CLASS = "com.fuzs.gamblingstyle.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "com.fuzs.gamblingstyle.proxy.ServerProxy";
    public static final String FINGERPRINT = "@FINGERPRINT@";

    public static final Logger LOGGER = LogManager.getLogger(GamblingStyle.NAME);

    @SidedProxy(clientSide = GamblingStyle.CLIENT_PROXY_CLASS, serverSide = GamblingStyle.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        NetworkHandler.init();
    }

    @EventHandler
    public void fingerprintViolation(FMLFingerprintViolationEvent event) {
        LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }
}
