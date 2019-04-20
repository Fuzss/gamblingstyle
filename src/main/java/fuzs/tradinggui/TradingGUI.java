package fuzs.tradinggui;

import fuzs.tradinggui.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TradingGUI.MODID, name = TradingGUI.NAME, version = TradingGUI.VERSION)
public class TradingGUI
{
    public static final String MODID = "tradinggui";
    public static final String NAME = "Gambling Style";
    public static final String VERSION = "1.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new fuzs.tradinggui.handlers.EventHandler());
        NetworkHandler.init();
    }
}
