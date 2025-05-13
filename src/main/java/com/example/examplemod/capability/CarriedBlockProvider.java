package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

public class CarriedBlockProvider implements ICapabilitySerializable<CompoundNBT> {

    @CapabilityInject(ICarriedBlock.class)
    public static Capability<ICarriedBlock> CAP = null;   // Forge が注入

    /*―― ① 保持インスタンス ――*/
    private final ICarriedBlock impl = new ICarriedBlock() {
        private final CarriedBlockData data = new CarriedBlockData();
        @Override public CarriedBlockData getData() { return data; }
    };
    private final LazyOptional<ICarriedBlock> lazy = LazyOptional.of(() -> impl);

    /*―― ② Capability 取得 ――*/
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, net.minecraft.util.Direction side) {
        return cap == CAP ? lazy.cast() : LazyOptional.empty();
    }

    /*―― ③ NBT Save / Load ――*/
    @Override public CompoundNBT serializeNBT() {
        INBT nbt = CAP.getStorage().writeNBT(CAP, impl, null);
        return nbt instanceof CompoundNBT ? (CompoundNBT) nbt : new CompoundNBT();
    }
    @Override public void deserializeNBT(CompoundNBT nbt) {
        CAP.getStorage().readNBT(CAP, impl, null, nbt);
    }

    /*―― ④ 登録メソッド（ExampleMod.setup 内から呼ぶ） ――*/
    public static void register() {
        CapabilityManager.INSTANCE.register(
                ICarriedBlock.class,
                new CarriedBlockStorage(),
                () -> new ICarriedBlock() {            // デフォルトインスタンス
                    private final CarriedBlockData d = new CarriedBlockData();
                    @Override public CarriedBlockData getData() { return d; }
                }
        );
    }
}
