package com.fuzs.gamblingstyle.capability.core;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * serializer for capability, has to be provided when registering
 * @param <T> capability class
 */
public class CapabilityStorage<T extends INBTSerializable<NBTTagCompound>> implements Capability.IStorage<T> {

    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {

        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

        if (nbt instanceof NBTTagCompound) {

            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }

}