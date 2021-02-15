package com.fuzs.gamblingstyle.handler;

import com.fuzs.gamblingstyle.capability.CapabilityController;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.message.OpenWindowMessage;
import com.fuzs.gamblingstyle.network.message.TradingListMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OpenGuiHandler {

    @SuppressWarnings({"unused", "unchecked"})
    @SubscribeEvent
    public <T extends EntityLivingBase & IMerchant> void onPlayerInteract(final PlayerInteractEvent.EntityInteract evt) {

        if (evt.getTarget() instanceof EntityLivingBase && evt.getTarget() instanceof IMerchant) {

            T merchant = (T) evt.getTarget();
            EntityPlayer player = evt.getEntityPlayer();
            ItemStack itemstack = evt.getItemStack();
            if (itemstack.getItem() != Items.NAME_TAG && !this.holdingSpawnEggOfClass(itemstack, merchant.getClass())) {

                if (merchant.isEntityAlive() && merchant.getCustomer() == null && !player.isSneaking() && !merchant.isChild()) {

                    if (this.tryOpenTradeGui(player, merchant, evt.getHand())) {

                        evt.setCancellationResult(EnumActionResult.SUCCESS);
                        evt.setCanceled(true);
                    }
                }
            }
        }
    }

    private <T extends EntityLivingBase & IMerchant> boolean tryOpenTradeGui(EntityPlayer player, T merchant, EnumHand hand) {

        if (hand == EnumHand.MAIN_HAND) {

            player.addStat(StatList.TALKED_TO_VILLAGER);
        }

        MerchantRecipeList merchantrecipelist = merchant.getRecipes(player);
        if (merchantrecipelist != null) {

            if (!merchant.world.isRemote && !merchantrecipelist.isEmpty()) {

                merchant.setCustomer(player);
                this.displayVillagerTradeGui((EntityPlayerMP) player, merchant, merchantrecipelist);

                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given item is a spawn egg that spawns the given class of entity.
     */
    @SuppressWarnings("ConstantConditions")
    private boolean holdingSpawnEggOfClass(ItemStack stack, Class<? extends Entity> entityClass) {

        if (stack.getItem() != Items.SPAWN_EGG) {

            return false;
        } else {

            Class<? extends Entity> clazz = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));
            return clazz != null && entityClass == clazz;
        }
    }

    private <T extends EntityLivingBase & IMerchant> void displayVillagerTradeGui(EntityPlayerMP player, T merchant, MerchantRecipeList merchantrecipelist) {

        player.getNextWindowId();
        player.openContainer = new ContainerVillager(player.inventory, merchant, player.world);
        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.addListener(player);
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
        IInventory iinventory = ((ContainerVillager) player.openContainer).getMerchantInventory();
        ITextComponent itextcomponent = ((IMerchant) merchant).getDisplayName();

        ITradingInfo tradingInfo = CapabilityController.getCapability(merchant, CapabilityController.TRADING_INFO_CAPABILITY);
        NetworkHandler.get().sendTo(new OpenWindowMessage(player.currentWindowId, itextcomponent, iinventory.getSizeInventory(),
                merchant.getEntityId(), tradingInfo.getLastTradeIndex(), tradingInfo.getFilterMode(), tradingInfo.getFavoriteTrades()), player);

        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeInt(player.currentWindowId);
        merchantrecipelist.writeToBuf(packetbuffer);
        NetworkHandler.get().sendTo(new TradingListMessage(packetbuffer), player);
    }

}
