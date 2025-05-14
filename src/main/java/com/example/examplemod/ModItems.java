package com.example.examplemod;

import com.example.examplemod.item.HeldBlockIndicatorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static Item HELD_BLOCK_INDICATOR;

    @SubscribeEvent
    public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
        HELD_BLOCK_INDICATOR = new HeldBlockIndicatorItem(
                new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)
        ).setRegistryName(new ResourceLocation(ExampleMod.MOD_ID, "held_block_indicator"));

        event.getRegistry().register(HELD_BLOCK_INDICATOR);
    }
}
