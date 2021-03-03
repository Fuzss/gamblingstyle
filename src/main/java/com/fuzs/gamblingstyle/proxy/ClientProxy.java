package com.fuzs.gamblingstyle.proxy;

import com.fuzs.gamblingstyle.client.handler.OpenGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class ClientProxy implements IProxy {

    @Nonnull
    @Override
    public IThreadListener getInstance(@Nullable IThreadListener threadListener) {

        return threadListener != null ? threadListener : Minecraft.getMinecraft();
    }

    @Nonnull
    @Override
    public EntityPlayer getPlayer(@Nullable EntityPlayerMP player) {

        return player != null ? player : Minecraft.getMinecraft().player;
    }

    @Override
    public void onSidedInit(FMLInitializationEvent evt) {

        MinecraftForge.EVENT_BUS.register(new OpenGuiHandler());
    }

}
