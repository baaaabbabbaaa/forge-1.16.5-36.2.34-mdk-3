package com.example.examplemod;

import com.example.examplemod.capability.CarriedBlockProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod("examplemod") // あなたのMOD IDに合わせて変更
public class ExampleMod {
    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // セットアップイベントに登録
        modEventBus.addListener(this::setup);

        // 通常イベント（AttachCapabilities など）も登録
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CarriedBlockProvider.register(); // Capability 登録！
    }
}
