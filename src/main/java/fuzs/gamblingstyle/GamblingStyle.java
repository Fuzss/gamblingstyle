package fuzs.gamblingstyle;

import fuzs.gamblingstyle.network.NetworkHandler;
import fuzs.gamblingstyle.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = GamblingStyle.MODID,
        name = GamblingStyle.NAME,
        version = GamblingStyle.VERSION,
        acceptedMinecraftVersions = GamblingStyle.RANGE,
        dependencies = GamblingStyle.DEPENDENCIES
)
public class GamblingStyle
{
    public static final String MODID = "gamblingstyle";
    public static final String NAME = "Gambling Style";
    public static final String VERSION = "1.0";
    public static final String RANGE = "[1.12.2]";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,)";
    public static final String CLIENT_PROXY_CLASS = "fuzs.gamblingstyle.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "fuzs.gamblingstyle.proxy.ServerProxy";

    @SidedProxy(clientSide = GamblingStyle.CLIENT_PROXY_CLASS, serverSide = GamblingStyle.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new fuzs.gamblingstyle.handler.EventHandler());
        NetworkHandler.init();
    }
}
