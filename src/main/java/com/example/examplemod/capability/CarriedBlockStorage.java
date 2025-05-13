package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CarriedBlockStorage implements IStorage<ICarriedBlock> {

    @Override
    public INBT writeNBT(Capability<ICarriedBlock> cap, ICarriedBlock inst, net.minecraft.util.Direction side) {
        return inst.getData().serializeNBT();
    }

    @Override
    public void readNBT(Capability<ICarriedBlock> cap, ICarriedBlock inst, net.minecraft.util.Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) inst.getData().deserializeNBT((CompoundNBT) nbt);
    }
}
