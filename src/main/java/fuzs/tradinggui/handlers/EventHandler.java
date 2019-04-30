package fuzs.tradinggui.handlers;

import fuzs.tradinggui.inventory.ContainerVillager;
import fuzs.tradinggui.network.NetworkHandler;
import fuzs.tradinggui.network.messages.MessageOpenWindow;
import fuzs.tradinggui.network.messages.MessageTradingList;
import fuzs.tradinggui.util.IPrivateAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler implements IPrivateAccessor {

    @SubscribeEvent
    public void interact(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof EntityVillager) {

            World worldIn = evt.getWorld();
            EntityVillager entityVillager = (EntityVillager) evt.getTarget();
            EntityPlayer player = evt.getEntityPlayer();
            ItemStack itemstack = evt.getItemStack();

            boolean flag = itemstack.getItem() == Items.NAME_TAG;

            if (!flag) {
                if (!this.holdingSpawnEggOfClass(itemstack, entityVillager.getClass()) && entityVillager.isEntityAlive()
                        && !entityVillager.isTrading() && !entityVillager.isChild() && !player.isSneaking()) {
                    if (!worldIn.isRemote)
                    {
                        evt.setCanceled(true);
                        this.displayVillagerTradeGui((EntityPlayerMP) player, entityVillager);
                    }
                }

            }
        }
    }

    /**
     * Checks if the given item is a spawn egg that spawns the given class of entity.
     */
    private boolean holdingSpawnEggOfClass(ItemStack stack, Class <? extends Entity> entityClass)
    {
        if (stack.getItem() != Items.SPAWN_EGG)
        {
            return false;
        }
        else
        {
            Class <? extends Entity > oclass = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));
            return oclass != null && entityClass == oclass;
        }
    }

    private void displayVillagerTradeGui(EntityPlayerMP player, EntityVillager villager)
    {
        MerchantRecipeList merchantrecipelist = ((IMerchant) villager).getRecipes(player);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {

            villager.setCustomer(player);
            player.getNextWindowId();
            player.openContainer = new ContainerVillager(player.inventory, villager, player.world);
            player.openContainer.windowId = player.currentWindowId;
            player.openContainer.addListener(player);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                    new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
            IInventory iinventory = ((ContainerVillager) player.openContainer).getMerchantInventory();
            ITextComponent itextcomponent = ((IMerchant) villager).getDisplayName();

            int wealth = this.getWealth(villager);
            NetworkHandler.sendTo(new MessageOpenWindow(player.currentWindowId, itextcomponent, iinventory.getSizeInventory(),
                    villager.getEntityId(), wealth < merchantrecipelist.size() && wealth >= 0 ? wealth : 0), player);

            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(player.currentWindowId);
            merchantrecipelist.writeToBuf(packetbuffer);
            NetworkHandler.sendTo(new MessageTradingList(packetbuffer), player);
        }
    }

}
