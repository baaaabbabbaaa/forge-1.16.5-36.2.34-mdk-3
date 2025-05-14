package com.example.examplemod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class HeldBlockIndicatorItem extends Item {
    public HeldBlockIndicatorItem(Properties props) {
        super(props);
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        // 設置不可
        return ActionResultType.FAIL;
    }

    @Override
    public boolean canAttackBlock(net.minecraft.block.BlockState state, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.player.PlayerEntity player) {
        // 壊すこともできない
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // エンチャント風キラキラ表示（好みに応じて）
    }
}
