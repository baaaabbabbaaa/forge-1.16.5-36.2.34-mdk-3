package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CarriedBlockData {
    private ResourceLocation blockID = null;
    private CompoundNBT blockNBT = null;

    public void set(ResourceLocation id, CompoundNBT nbt) {
        this.blockID = id;
        this.blockNBT = nbt;
    }

    public void clear() {
        this.blockID = null;
        this.blockNBT = null;
    }

    public boolean hasBlock() {
        return blockID != null && blockNBT != null;
    }

    public ResourceLocation getBlockID() {
        return blockID;
    }

    public CompoundNBT getBlockNBT() {
        return blockNBT;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        if (blockID != null && blockNBT != null) {
            tag.putString("id", blockID.toString());
            tag.put("data", blockNBT);
        }
        return tag;
    }

    public void deserializeNBT(CompoundNBT tag) {
        if (tag.contains("id") && tag.contains("data")) {
            this.blockID = new ResourceLocation(tag.getString("id"));
            this.blockNBT = tag.getCompound("data");
        }
    }
}
