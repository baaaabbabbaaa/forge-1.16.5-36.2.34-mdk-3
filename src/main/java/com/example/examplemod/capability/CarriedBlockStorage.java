package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CarriedBlockStorage implements IStorage<ICarriedBlock> {

    @Override
    public INBT writeNBT(Capability<ICarriedBlock> capability, ICarriedBlock instance, net.minecraft.util.Direction side) {
        return instance.getData().serializeNBT();
    }

    @Override
    public void readNBT(Capability<ICarriedBlock> capability, ICarriedBlock instance, net.minecraft.util.Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.getData().deserializeNBT((CompoundNBT) nbt);
        }
    }
}
