package com.example.examplemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(ExampleMod.MOD_ID)                       // mods.toml の modid と合わせる
public class ExampleMod {
    public static final String MOD_ID = "examplemod";

    public ExampleMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);     // ← 共通セットアップ

        // Forge 共通イベントバスに自作ハンドラを登録
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
    }

    /** Forge 共通セットアップ */
    private void setup(final FMLCommonSetupEvent evt) {
        // Capability 登録を “遅延ワーク” で必ず呼び出す
        evt.enqueueWork(() -> com.example.examplemod.capability.CarriedBlockProvider.register());
    }
}
