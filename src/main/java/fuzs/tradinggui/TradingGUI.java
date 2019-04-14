package fuzs.tradinggui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = TradingGUI.MODID, name = TradingGUI.NAME, version = TradingGUI.VERSION)
public class TradingGUI
{
    public static final String MODID = "tradinggui";
    public static final String NAME = "Trading GUI";
    public static final String VERSION = "1.0";

    private static final Minecraft MC = Minecraft.getMinecraft();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void openMerchantInterface(GuiOpenEvent evt) {
        if (evt.getGui() instanceof GuiMerchant) {
            IMerchant merchant = ((GuiMerchant) evt.getGui()).getMerchant();
            EntityPlayer entityPlayer = merchant.getCustomer();
            if (entityPlayer != null)
                evt.setGui(new GuiVillager(entityPlayer.inventory, merchant, merchant.getWorld()));
        }
    }
}
