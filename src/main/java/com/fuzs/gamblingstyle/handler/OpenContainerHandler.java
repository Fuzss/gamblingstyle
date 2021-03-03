package com.fuzs.gamblingstyle.handler;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.capability.CapabilityController;
import com.fuzs.gamblingstyle.capability.container.ITradingInfo;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.network.NetworkHandler;
import com.fuzs.gamblingstyle.network.message.SOpenWindowMessage;
import com.fuzs.gamblingstyle.network.message.STradingListMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class OpenContainerHandler {

    @SuppressWarnings({"unused", "unchecked"})
    @SubscribeEvent(priority = EventPriority.HIGH)
    public <T extends EntityLivingBase & IMerchant> void onContainerOpen(final PlayerContainerEvent.Open evt) {

        if (evt.getContainer() instanceof ContainerMerchant) {

            IMerchant merchant = this.getMerchant((ContainerMerchant) evt.getContainer());
            if (merchant instanceof EntityLivingBase && evt.getEntityPlayer() instanceof EntityPlayerMP) {

                EntityPlayerMP player = (EntityPlayerMP) evt.getEntityPlayer();
                MerchantRecipeList merchantrecipelist = merchant.getRecipes(evt.getEntityPlayer());
                if (merchantrecipelist != null && !merchantrecipelist.isEmpty() && merchant.getCustomer() == player) {

                    player.closeContainer();
                    // is reset on closing container
                    merchant.setCustomer(player);
                    this.displayVillagerTradeGui(player, (T) merchant);
                    this.sendTradingList(player, merchantrecipelist);
                }
            }
        }
    }

    @Nullable
    private IMerchant getMerchant(ContainerMerchant container) {

        try {

            return ObfuscationReflectionHelper.getPrivateValue(ContainerMerchant.class, container, "field_75178_e");
        } catch (Exception e) {

            GamblingStyle.LOGGER.error("getMerchant() failed", e);
        }

        return null;
    }

    private <T extends EntityLivingBase & IMerchant> void displayVillagerTradeGui(EntityPlayerMP player, T merchant) {

        player.getNextWindowId();
        player.openContainer = new ContainerVillager(player.inventory, merchant, player.world);
        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.addListener(player);
        IInventory iinventory = ((ContainerVillager) player.openContainer).getMerchantInventory();
        ITextComponent itextcomponent = merchant.getDisplayName();

        ITradingInfo tradingInfo = CapabilityController.getCapability(merchant, CapabilityController.TRADING_INFO_CAPABILITY);
        NetworkHandler.get().sendTo(new SOpenWindowMessage(player.currentWindowId, itextcomponent, iinventory.getSizeInventory(),
                merchant.getEntityId(), tradingInfo.getLastTradeIndex(), tradingInfo.getFilterMode(), tradingInfo.getFavoriteTrades()), player);
    }

    private void sendTradingList(EntityPlayerMP player, MerchantRecipeList merchantrecipelist) {

        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeInt(player.currentWindowId);
        merchantrecipelist.writeToBuf(packetbuffer);
        NetworkHandler.get().sendTo(new STradingListMessage(packetbuffer), player);
    }

}
