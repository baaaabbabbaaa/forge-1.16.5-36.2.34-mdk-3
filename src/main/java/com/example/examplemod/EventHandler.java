package com.example.examplemod;

import com.example.examplemod.capability.CarriedBlockProvider;
import com.example.examplemod.capability.ICarriedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(new ResourceLocation(ExampleMod.MOD_ID, "carried_block"), new CarriedBlockProvider());
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.player.level.isClientSide) return;
        e.player.getCapability(CarriedBlockProvider.CAP).ifPresent(cap -> {
            if (cap.hasBlock()) {
                e.player.setSprinting(false);
                // 見た目に手に何かを持っているようにする
                if (e.player.getMainHandItem().isEmpty()) {
                    e.player.setItemInHand(Hand.MAIN_HAND, new ItemStack(ModItems.HELD_BLOCK_INDICATOR));
                }
            } else {
                if (e.player.getMainHandItem().getItem() == ModItems.HELD_BLOCK_INDICATOR) {
                    e.player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock ev) {
        if (ev.getWorld().isClientSide() || !ev.getPlayer().isCrouching()) return;

        PlayerEntity player = ev.getPlayer();
        World world = ev.getWorld();
        BlockPos clickedPos = ev.getPos();
        Direction face = ev.getFace() == null ? Direction.UP : ev.getFace();
        BlockPos placePos = clickedPos.relative(face);

        LazyOptional<ICarriedBlock> capOpt = player.getCapability(CarriedBlockProvider.CAP);
        if (!capOpt.isPresent()) return;
        ICarriedBlock cap = capOpt.orElseThrow(IllegalStateException::new);

        // 【運搬中 → 設置】
        if (cap.hasBlock()) {
            if (!world.getBlockState(placePos).getMaterial().isReplaceable()) {
                player.displayClientMessage(new StringTextComponent("そこには置けません"), true);
                return;
            }

            ResourceLocation id = cap.getData().getBlockID();
            CompoundNBT nbt = cap.getData().getBlockNBT();
            BlockState newState = ForgeRegistries.BLOCKS.getValue(id).defaultBlockState();

            world.setBlock(placePos, newState, 3);
            world.sendBlockUpdated(placePos, newState, newState, 11);

            nbt.putInt("x", placePos.getX());
            nbt.putInt("y", placePos.getY());
            nbt.putInt("z", placePos.getZ());
            TileEntity newTile = TileEntity.loadStatic(newState, nbt);
            if (newTile != null) {
                world.setBlockEntity(placePos, newTile);
                newTile.setChanged();
            }

            cap.clear();
            player.displayClientMessage(new StringTextComponent("ブロックを設置！"), true);
            ev.setCanceled(true);
            ev.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        // 【持ち上げ】
        BlockState clickedState = world.getBlockState(clickedPos);
        if (!clickedState.hasTileEntity()) return;

        TileEntity tile = world.getBlockEntity(clickedPos);
        if (tile == null) return;

        CompoundNBT saved = tile.save(new CompoundNBT());
        ResourceLocation id = clickedState.getBlock().getRegistryName();
        cap.getData().set(id, saved);

        world.removeBlockEntity(clickedPos);
        world.setBlock(clickedPos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);

        player.displayClientMessage(new StringTextComponent("ブロックを持ち上げた！"), true);
        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.SUCCESS);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof PlayerEntity) || e.getEntity().level.isClientSide()) return;

        PlayerEntity player = (PlayerEntity) e.getEntity();
        World world = player.level;

        LazyOptional<ICarriedBlock> capOpt = player.getCapability(CarriedBlockProvider.CAP);
        if (!capOpt.isPresent()) return;
        ICarriedBlock cap = capOpt.orElseThrow(IllegalStateException::new);

        if (!cap.hasBlock()) return;

        ResourceLocation id = cap.getData().getBlockID();
        CompoundNBT nbt = cap.getData().getBlockNBT();
        BlockState state = ForgeRegistries.BLOCKS.getValue(id).defaultBlockState();

        BlockPos pos = player.blockPosition();
        world.setBlock(pos, state, 3);
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        TileEntity tile = TileEntity.loadStatic(state, nbt);
        if (tile != null) {
            world.setBlockEntity(pos, tile);
            tile.setChanged();
        }

        cap.clear();
    }
}
