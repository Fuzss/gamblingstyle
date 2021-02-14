package com.fuzs.gamblingstyle;

import com.fuzs.gamblingstyle.handler.OpenGuiHandler;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.message.MessageOpenWindow;
import com.fuzs.gamblingstyle.network.message.MessageTradingData;
import com.fuzs.gamblingstyle.network.message.MessageTradingList;
import com.fuzs.gamblingstyle.proxy.CommonProxy;
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
        version = "1.1",
        acceptedMinecraftVersions = "[1.12.2]"
)
public class GamblingStyle {

    public static final String MODID = "gamblingstyle";
    public static final String NAME = "Gambling Style";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final String CLIENT_PROXY = "com.fuzs." + MODID + ".proxy.ClientProxy";
    private static final String COMMON_PROXY = "com.fuzs." + MODID + ".proxy.CommonProxy";

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent evt) {

        MinecraftForge.EVENT_BUS.register(new OpenGuiHandler());

        NetworkHandler.get().registerMessage(MessageOpenWindow.class, Side.CLIENT);
        NetworkHandler.get().registerMessage(MessageTradingList.class, Side.CLIENT);
        NetworkHandler.get().registerMessage(MessageTradingData.class, Side.SERVER);
    }

}
