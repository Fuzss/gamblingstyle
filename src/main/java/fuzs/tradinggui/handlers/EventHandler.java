package fuzs.tradinggui.handlers;

import fuzs.tradinggui.inventory.ContainerVillager;
import fuzs.tradinggui.network.NetworkHandler;
import fuzs.tradinggui.network.messages.MessageOpenWindow;
import fuzs.tradinggui.network.messages.MessageTradingList;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class EventHandler {

    @SubscribeEvent
    public void interact(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof EntityVillager) {
            evt.setCanceled(true);

            World worldIn = evt.getWorld();
            EntityVillager entityVillager = (EntityVillager) evt.getTarget();
            EntityPlayer player = evt.getEntityPlayer();

            if (!worldIn.isRemote)
            {
                entityVillager.setCustomer(player);
                this.displayVillagerTradeGui((EntityPlayerMP) player, entityVillager);
            }
        }
    }

    private void displayVillagerTradeGui(EntityPlayerMP player, EntityVillager villager)
    {
        player.getNextWindowId();
        player.openContainer = new ContainerVillager(player.inventory, villager, player.world);
        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.addListener(player);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
        IInventory iinventory = ((ContainerVillager) player.openContainer).getMerchantInventory();
        ITextComponent itextcomponent = ((IMerchant) villager).getDisplayName();
        NetworkHandler.sendTo(new MessageOpenWindow(player.currentWindowId, itextcomponent, iinventory.getSizeInventory(), villager.getEntityId()), player);

        MerchantRecipeList merchantrecipelist = ((IMerchant) villager).getRecipes(player);

        if (merchantrecipelist != null) {
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(player.currentWindowId);
            merchantrecipelist.writeToBuf(packetbuffer);
            NetworkHandler.sendTo(new MessageTradingList(packetbuffer), player);
        }
    }

}
