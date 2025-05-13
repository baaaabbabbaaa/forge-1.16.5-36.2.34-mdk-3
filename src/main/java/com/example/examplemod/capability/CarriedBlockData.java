package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class CarriedBlockData {
    private ResourceLocation blockID;   // 例：minecraft:chest
    private CompoundNBT      blockNBT;  // TileEntity の NBT

    /*―― setter / getter ――*/
    public void set(ResourceLocation id, CompoundNBT nbt) {
        blockID  = id;
        blockNBT = nbt;
    }
    public void clear() { blockID = null; blockNBT = null; }
    public boolean hasBlock()         { return blockID != null && blockNBT != null; }
    public ResourceLocation getBlockID() { return blockID; }
    public CompoundNBT getBlockNBT()     { return blockNBT; }

    /*―― Capability 保存 / 読込用 ――*/
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        if (hasBlock()) {
            tag.putString("id", blockID.toString());
            tag.put("data", blockNBT);
        }
        return tag;
    }
    public void deserializeNBT(CompoundNBT tag) {
        if (tag.contains("id") && tag.contains("data")) {
            blockID  = new ResourceLocation(tag.getString("id"));
            blockNBT = tag.getCompound("data");
        }
    }
}
