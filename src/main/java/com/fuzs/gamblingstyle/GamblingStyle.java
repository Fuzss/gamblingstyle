package com.fuzs.gamblingstyle;

import com.fuzs.gamblingstyle.capability.CapabilityController;
import com.fuzs.gamblingstyle.network.message.CMoveIngredientsMessage;
import com.fuzs.gamblingstyle.network.message.CSelectedRecipeMessage;
import com.fuzs.gamblingstyle.network.message.CSyncTradingInfoMessage;
import com.fuzs.gamblingstyle.handler.OpenGuiHandler;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.message.*;
import com.fuzs.gamblingstyle.proxy.IProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(
        modid = GamblingStyle.MODID,
        name = GamblingStyle.NAME,
        version = "1.1.1",
        acceptedMinecraftVersions = "[1.12.2]"
)
public class GamblingStyle {

    public static final String MODID = "gamblingstyle";
    public static final String NAME = "Gambling Style";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final String CLIENT_PROXY = "com.fuzs." + MODID + ".proxy.ClientProxy";
    private static final String SERVER_PROXY = "com.fuzs." + MODID + ".proxy.ServerProxy";

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static IProxy proxy;

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent evt) {

        this.registerMessages();
        MinecraftForge.EVENT_BUS.register(new CapabilityController());
        MinecraftForge.EVENT_BUS.register(new OpenGuiHandler());
    }

    private void registerMessages() {

        NetworkHandler.get().registerMessage(SOpenWindowMessage.class, Side.CLIENT);
        NetworkHandler.get().registerMessage(STradingListMessage.class, Side.CLIENT);
        NetworkHandler.get().registerMessage(CSyncTradingInfoMessage.class, Side.SERVER);
        NetworkHandler.get().registerMessage(CSelectedRecipeMessage.class, Side.SERVER);
        NetworkHandler.get().registerMessage(CMoveIngredientsMessage.class, Side.SERVER);
    }

}
