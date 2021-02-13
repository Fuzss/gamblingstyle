package com.fuzs.gamblingstyle.handler;

import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.messages.MessageOpenWindow;
import com.fuzs.gamblingstyle.network.messages.MessageTradingList;
import com.fuzs.gamblingstyle.util.IPrivateAccessor;

import org.apache.commons.lang3.BooleanUtils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
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
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModEventHandler implements IPrivateAccessor {

    @SubscribeEvent
    public void interact(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof IMerchant) {

            IMerchant entityVillager = (IMerchant) evt.getTarget();
            EntityPlayer player = evt.getEntityPlayer();
            ItemStack itemstack = evt.getItemStack();

            boolean flag = itemstack.getItem() == Items.NAME_TAG;

            if (!flag) {
                boolean trading;
                if (evt.getTarget() instanceof EntityVillager)
                    trading = ((EntityVillager) evt.getTarget()).isTrading();
                else {
                    trading = BooleanUtils.isTrue((boolean) this.invoke(
                            evt.getTarget().getClass(), 
                            evt.getTarget(), 
                            "isTrading", 
                            false, 
                            new Class[]{ void.class }, 
                            (Object[]) null));
                }

                if (!this.holdingSpawnEggOfClass(itemstack, evt.getTarget().getClass()) 
                        && evt.getTarget().isEntityAlive()
                        && !trading
                        && (evt.getTarget() instanceof EntityAgeable 
                                && !((EntityAgeable) evt.getTarget()).isChild()) 
                        && !player.isSneaking()) {
                    if (this.displayVillagerTradeGui(player, entityVillager))
                    {
                        if (evt.getHand() == EnumHand.MAIN_HAND)
                        {
                            player.addStat(StatList.TALKED_TO_VILLAGER);
                        }
                        evt.setCancellationResult(EnumActionResult.SUCCESS);
                        evt.setCanceled(true);
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

    private boolean displayVillagerTradeGui(EntityPlayer player, IMerchant villager)
    {
        MerchantRecipeList merchantrecipelist = villager.getRecipes(player);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                villager.setCustomer(playerMP);
                playerMP.getNextWindowId();
                playerMP.openContainer = new ContainerVillager(playerMP.inventory, villager, playerMP.world);
                playerMP.openContainer.windowId = playerMP.currentWindowId;
                playerMP.openContainer.addListener(playerMP);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                        new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(playerMP, playerMP.openContainer));
                IInventory iinventory = ((ContainerVillager) playerMP.openContainer).getMerchantInventory();
                ITextComponent itextcomponent = villager.getDisplayName();

                int wealth = (villager instanceof EntityVillager) ? ((EntityVillager) villager).wealth : 
                                this.getField(villager.getClass(), villager, "wealth", 0, false);
                NetworkHandler.sendTo(new MessageOpenWindow(playerMP.currentWindowId, itextcomponent, iinventory.getSizeInventory(),
                        ((Entity) villager).getEntityId(), wealth < merchantrecipelist.size() && wealth >= 0 ? wealth : 0), playerMP);

                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
                packetbuffer.writeInt(playerMP.currentWindowId);
                merchantrecipelist.writeToBuf(packetbuffer);
                NetworkHandler.sendTo(new MessageTradingList(packetbuffer), playerMP);
            } else if (player instanceof EntityPlayerSP) {
                player.swingArm(EnumHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

}
