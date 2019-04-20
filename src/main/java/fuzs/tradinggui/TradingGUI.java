package fuzs.tradinggui;

import fuzs.tradinggui.network.NetworkHandler;
import fuzs.tradinggui.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TradingGUI.MODID, name = TradingGUI.NAME, version = TradingGUI.VERSION)
public class TradingGUI
{
    public static final String MODID = "tradinggui";
    public static final String NAME = "Gambling Style";
    public static final String VERSION = "1.0";
    public static final String CLIENT_PROXY_CLASS = "fuzs.tradinggui.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "fuzs.tradinggui.proxy.ServerProxy";

    @SidedProxy(clientSide = TradingGUI.CLIENT_PROXY_CLASS, serverSide = TradingGUI.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new fuzs.tradinggui.handlers.EventHandler());
        NetworkHandler.init();
    }
}
