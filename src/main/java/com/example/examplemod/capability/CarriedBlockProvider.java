package com.example.examplemod.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CarriedBlockProvider implements ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(ICarriedBlock.class)
    public static Capability<ICarriedBlock> CARRIED_BLOCK_CAPABILITY = null;

    private final ICarriedBlock instance = new ICarriedBlock() {
        private final CarriedBlockData data = new CarriedBlockData();
        @Override
        public CarriedBlockData getData() {
            return data;
        }
    };

    private final LazyOptional<ICarriedBlock> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CARRIED_BLOCK_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        INBT rawNbt = CARRIED_BLOCK_CAPABILITY.getStorage().writeNBT(CARRIED_BLOCK_CAPABILITY, instance, null);
        if (rawNbt instanceof CompoundNBT) {
            return (CompoundNBT) rawNbt;
        } else {
            return new CompoundNBT();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CARRIED_BLOCK_CAPABILITY.getStorage().readNBT(CARRIED_BLOCK_CAPABILITY, instance, null, nbt);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(
                ICarriedBlock.class,
                new CarriedBlockStorage(),
                () -> new ICarriedBlock() {
                    private final CarriedBlockData data = new CarriedBlockData();
                    @Override
                    public CarriedBlockData getData() {
                        return data;
                    }
                }
        );

        System.out.println("[DEBUG] Capability register 実行");
        System.out.println("[DEBUG] CARRIED_BLOCK_CAPABILITY = " + CARRIED_BLOCK_CAPABILITY);
    }


    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CarriedBlockProvider.register(); // ← これが "後で実行される" ように！
        });
    }
}
