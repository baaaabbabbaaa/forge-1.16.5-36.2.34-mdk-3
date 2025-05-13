package com.example.examplemod;

import com.example.examplemod.capability.CarriedBlockProvider;
import com.example.examplemod.capability.ICarriedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class EventHandler {

    public static final ResourceLocation CARRIED_BLOCK_ID = new ResourceLocation("examplemod", "carried_block");

    // Capability 付与
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            System.out.println("[DEBUG] AttachCapabilitiesEvent<Entity> プレイヤー発見！");

            event.addCapability(CARRIED_BLOCK_ID, new CarriedBlockProvider());
        }
    }

    // ブロック持ち上げ処理
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return; // ← これ！
        System.out.println("[DEBUG] RightClickBlock triggered");
        if (event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.DENY) return; // ← 追加！
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        // 条件：サーバー側、スニーク状態
        if (world.isClientSide()) {
            System.out.println("[DEBUG] クライアント側なので return");
            return;
        }

        if (!player.isCrouching()) {
            System.out.println("[DEBUG] スニークしてないので return");
            return;
        }


        event.setUseBlock(Event.Result.DENY);

        // TileEntity保持ブロックのみ対象
        if (!state.hasTileEntity()) return;

        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity == null) return;

        // Capability取得
        // Capability取得して handler を取得
        LazyOptional<ICarriedBlock> cap = player.getCapability(CarriedBlockProvider.CARRIED_BLOCK_CAPABILITY);
        System.out.println("[DEBUG] capability取得直後: isPresent = " + cap.isPresent());
        if (cap.isPresent()) {
            ICarriedBlock handler = cap.orElse(() -> {
                throw new IllegalStateException("CarriedBlock Capability not present!");
            });

            if (handler.hasBlock()) {
                if (!world.getBlockState(pos).getMaterial().isReplaceable()) {
                    player.displayClientMessage(new StringTextComponent("そこには置けません！"), true);
                    return;
                }

                ResourceLocation blockID = handler.getData().getBlockID();
                CompoundNBT nbt = handler.getData().getBlockNBT();

                BlockState newState = ForgeRegistries.BLOCKS
                        .getValue(blockID)
                        .defaultBlockState();

                world.setBlock(pos, newState, 3);

                nbt.putInt("x", pos.getX());
                nbt.putInt("y", pos.getY());
                nbt.putInt("z", pos.getZ());

                TileEntity newTile = TileEntity.loadStatic(newState, nbt);
                if (newTile != null) {
                    world.setBlockEntity(pos, newTile);
                    newTile.setChanged();
                    System.out.println("[DEBUG] 復元成功！");
                } else {
                    System.out.println("[DEBUG] TileEntity生成失敗！");
                }


                handler.clear();
                player.displayClientMessage(new StringTextComponent("ブロックを設置した！"), true);

                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
                return;
            } else {
                System.out.println("[DEBUG] 何も持ってないので持ち上げ処理に進む");

                CompoundNBT tileNBT = tileEntity.save(new CompoundNBT());
                System.out.println("[DEBUG] tileNBT内容 = " + tileNBT); // ← これ！
                ResourceLocation blockID = state.getBlock().getRegistryName();

                System.out.println("[DEBUG] blockID = " + blockID);
                System.out.println("[DEBUG] tileNBT = " + tileNBT);

                handler.getData().set(blockID, tileNBT);
                System.out.println("[DEBUG] set() 完了！");
                System.out.println("[DEBUG] hasBlock = " + handler.hasBlock());

                // 先に TileEntity を明示的に除去！
                world.removeBlockEntity(pos);
                // そのあとブロックをエアにする（中身ドロップさせない）
                world.setBlock(pos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);

                player.displayClientMessage(new StringTextComponent("ブロックを持ち上げた！"), true);

                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
            }
        }

        event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        PlayerEntity player = event.player;

        if (player.level.isClientSide()) return;

        player.getCapability(CarriedBlockProvider.CARRIED_BLOCK_CAPABILITY).ifPresent(handler -> {
            if (handler.hasBlock()) {
                System.out.println("[TICK] プレイヤーが何か持ってる！");
            }
        });
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        oldPlayer.getCapability(CarriedBlockProvider.CARRIED_BLOCK_CAPABILITY).ifPresent(oldCap -> {
            newPlayer.getCapability(CarriedBlockProvider.CARRIED_BLOCK_CAPABILITY).ifPresent(newCap -> {
                newCap.getData().set(oldCap.getData().getBlockID(), oldCap.getData().getBlockNBT());
                System.out.println("[DEBUG] Capability クローン成功");
            });
        });
    }
}

