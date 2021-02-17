package moe.lilybeevee.bitexchange.item;

import moe.lilybeevee.bitexchange.api.BitStorageItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class BitArrayItem extends Item implements BitStorageItem {
    public BitArrayItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        return setBits(stack, 0);
    }

    @Override
    public long getBits(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Bits")) {
            tag.putLong("Bits", 0);
            return 0;
        } else {
            return tag.getLong("Bits");
        }
    }

    @Override
    public ItemStack setBits(ItemStack stack, long count) {
        ItemStack newStack = stack.copy();
        newStack.getOrCreateTag().putLong("Bits", count);
        return newStack;
    }
}
