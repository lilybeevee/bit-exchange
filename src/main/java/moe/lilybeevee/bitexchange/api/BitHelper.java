package moe.lilybeevee.bitexchange.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

public class BitHelper {
    public static Pair<ItemStack, Long> convertToBits(ItemStack stack) {
        return BitHelper.convertToBits(stack, Long.MAX_VALUE);
    }
    public static Pair<ItemStack, Long> convertToBits(ItemStack stack, long max) {
        Item item = stack.getItem();
        long value = BitRegistry.get(item);
        if (item instanceof BitStorageItem) {
            BitStorageItem bitStorageItem = (BitStorageItem)item;
            ItemStack newStack = stack.copy();
            long amount = Math.min(max, bitStorageItem.getBits(newStack));
            return new Pair<>(bitStorageItem.takeBits(newStack, amount), amount);
        } else if (value > 0) {
            long amount = Math.min(stack.getCount() * value, max);
            int count = (int)Math.floorDiv(amount, value);
            ItemStack newStack = stack.copy();
            newStack.setCount(stack.getCount() - count);
            return new Pair<>(newStack, count * value);
        }
        return new Pair<>(stack, (long)0);
    }

    public static Pair<ItemStack, Long> createStack(Item item, long bits) {
        return BitHelper.createStack(item.getDefaultStack(), bits);
    }
    public static Pair<ItemStack, Long> createStack(Item item, long bits, int max) {
        return BitHelper.createStack(item.getDefaultStack(), bits, max);
    }
    public static Pair<ItemStack, Long> createStack(ItemStack stack, long bits) {
        return BitHelper.createStack(stack, bits, stack.getMaxCount());
    }
    public static Pair<ItemStack, Long> createStack(ItemStack stack, long bits, int max) {
        Item item = stack.getItem();
        long value = BitRegistry.get(item);
        if (value > 0) {
            int count = Math.min((int)Math.floorDiv(bits, value), Math.min(max, stack.getMaxCount()));
            if (count == 0) {
                return new Pair<>(ItemStack.EMPTY, (long)0);
            } else {
                ItemStack newStack = stack.copy();
                newStack.setCount(count);
                return new Pair<>(newStack, count * value);
            }
        }
        return new Pair<>(ItemStack.EMPTY, (long)0);
    }
}
