package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.GamblingStyle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.function.Consumer;

public abstract class Message<T extends IMessage> implements IMessage, IMessageHandler<T, T> {

    @Override
    public T onMessage(T message, MessageContext ctx) {

        if (ctx.side == Side.CLIENT) {

            handleClientSide(message, GamblingStyle.proxy.getClientPlayer());
        } else {

            handleServerSide(message, ctx.getServerHandler().player);
        }
        
        return null;
    }

    /**
     * Handle a packet on the client side. Note this occurs after decoding has completed.
     *
     * @param message
     * @param player  the player reference
     */
    public abstract void handleClientSide(T message, EntityPlayer player);

    /**
     * Handle a packet on the server side. Note this occurs after decoding has completed.
     *
     * @param message
     * @param player  the player reference
     */
    public abstract void handleServerSide(T message, EntityPlayer player);

    /**
     * @return message processor to run when received
     */
    protected abstract MessageProcessor createProcessor();

    /**
     * separate class for executing message when received to work around sided limitations
     */
    @FunctionalInterface
    protected interface MessageProcessor extends Consumer<EntityPlayer> {

    }

}
