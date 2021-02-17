package moe.lilybeevee.bitexchange.api;

import net.minecraft.item.ItemStack;

public interface BitStorageItem {
    long getBits(ItemStack stack);
    ItemStack setBits(ItemStack stack, long count);

    default long getMaxBits(ItemStack stack) { return Long.MAX_VALUE; }
    default boolean displayMaxBits(ItemStack stack) { return false; }

    default ItemStack takeBits(ItemStack stack, long count) {
        return setBits(stack, getBits(stack) - count);
    }

    default ItemStack addBits(ItemStack stack, long count) {
        return setBits(stack, getBits(stack) + count);
    }
}
