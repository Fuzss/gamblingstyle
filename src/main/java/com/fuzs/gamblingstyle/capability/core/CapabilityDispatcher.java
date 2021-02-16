package com.fuzs.gamblingstyle.capability.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * dispatcher for this serializable capability
 * @param <T> capability class
 */
public class CapabilityDispatcher<T extends INBTSerializable<NBTTagCompound>> implements ICapabilitySerializable<NBTTagCompound> {

    /**
     * capability wrapper for object
     */
    private final Capability<T> capability;
    /**
     * capability object
     */
    private final T storage;

    /**
     * @param storage object
     * @param capability wrapper
     */
    public CapabilityDispatcher(T storage, Capability<T> capability) {

        this.storage = storage;
        this.capability = capability;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {

        return capability == this.capability;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getCapability(@Nonnull Capability<S> capability, EnumFacing facing) {

        return capability == this.capability ? (S) this.storage : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {

        return this.storage.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

        this.storage.deserializeNBT(nbt);
    }

}