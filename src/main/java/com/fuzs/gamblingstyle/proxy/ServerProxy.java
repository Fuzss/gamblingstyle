package com.fuzs.gamblingstyle.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ServerProxy implements IProxy {

    @Nonnull
    @Override
    public IThreadListener getInstance(IThreadListener threadListener) {

        return threadListener;
    }

    @Nonnull
    @Override
    public EntityPlayer getPlayer(EntityPlayerMP player) {

        return player;
    }

}