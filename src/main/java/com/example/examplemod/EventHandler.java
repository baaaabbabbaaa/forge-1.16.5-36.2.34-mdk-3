package com.example.examplemod;

import com.example.examplemod.capability.CarriedBlockProvider;
import com.example.examplemod.capability.ICarriedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class EventHandler {

    /* Capability をプレイヤーに添付 */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity)
            e.addCapability(new ResourceLocation(ExampleMod.MOD_ID, "carried_block"),
                    new CarriedBlockProvider());
    }

    /* Sprint 禁止（運搬中） */
    @SubscribeEvent
    public static void onTick(PlayerTickEvent e) {
        if (e.player.level.isClientSide()) return;
        e.player.getCapability(CarriedBlockProvider.CAP).ifPresent(c -> {
            if (c.hasBlock() && e.player.isSprinting()) e.player.setSprinting(false);
        });
    }

    /*―― 右クリック：持ち上げ / 設置 ――*/
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick(PlayerInteractEvent.RightClickBlock ev) {

        if (ev.getWorld().isClientSide() || !ev.getPlayer().isCrouching()) return;

        PlayerEntity player = ev.getPlayer();
        World  world        = ev.getWorld();
        BlockPos clickedPos = ev.getPos();
        Direction face      = ev.getFace()==null ? Direction.UP : ev.getFace();
        BlockPos placePos   = clickedPos.relative(face);          // ← 置く座標

        BlockState clickedState = world.getBlockState(clickedPos);

        TileEntity tile = world.getBlockEntity(clickedPos);
        if (tile == null) return;

        /* Capability 取得 */
        LazyOptional<ICarriedBlock> capOpt = player.getCapability(CarriedBlockProvider.CAP);
        if (!capOpt.isPresent()) return;
        ICarriedBlock cap = capOpt.orElseThrow(IllegalStateException::new);

        /*==================== ① 既に運搬中 → 設置 ====================*/
        if (cap.hasBlock()) {

            if (!world.getBlockState(placePos).getMaterial().isReplaceable()) {
                player.displayClientMessage(new StringTextComponent("そこには置けません"), true);
                return;
            }

            ResourceLocation id   = cap.getData().getBlockID();
            CompoundNBT     nbt   = cap.getData().getBlockNBT();
            BlockState      newSt = ForgeRegistries.BLOCKS.getValue(id).defaultBlockState();

            /* ★ ここを 1 回だけ ―― flags = 11 (3|8) で確実に同期 */
            boolean ok = world.setBlock(placePos, newSt, 11);
            System.out.println("[DEBUG] setBlock returned = " + ok +
                    "  state=" + world.getBlockState(placePos));
            world.sendBlockUpdated(placePos, newSt, newSt, 11);

            /* TileEntity を NBT から復元 */
            nbt.putInt("x", placePos.getX());
            nbt.putInt("y", placePos.getY());
            nbt.putInt("z", placePos.getZ());
            TileEntity newTile = TileEntity.loadStatic(newSt, nbt);
            if (newTile != null) {
                world.setBlockEntity(placePos, newTile);
                newTile.setChanged();
            }

            cap.clear();
            player.displayClientMessage(new StringTextComponent("ブロックを設置！"), true);
            ev.setCancellationResult(ActionResultType.SUCCESS);
            ev.setCanceled(true);
            return;
        }

        /*==================== ② 未運搬 → 持ち上げ ====================*/
        if (!clickedState.hasTileEntity()) return; // ← ここに移動
        CompoundNBT saved = tile.save(new CompoundNBT());
        ResourceLocation id = clickedState.getBlock().getRegistryName();

        cap.getData().set(id, saved);

        /* ドロップ無で除去 */
        world.removeBlockEntity(clickedPos);
        world.setBlock(clickedPos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);

        player.displayClientMessage(new StringTextComponent("ブロックを持ち上げた！"), true);

        ev.setCancellationResult(ActionResultType.SUCCESS);
        ev.setCanceled(true);
    }
}
