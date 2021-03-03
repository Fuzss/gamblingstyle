package com.fuzs.gamblingstyle.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IProxy {

    /**
     * @return Minecraft client or server instance
     */
    @Nonnull
    IThreadListener getInstance(@Nullable IThreadListener threadListener);

    /**
     * @return player entity depending on side
     */
    @Nonnull
    EntityPlayer getPlayer(@Nullable EntityPlayerMP player);

    default void onSidedInit(final FMLInitializationEvent evt) {

    }

}
