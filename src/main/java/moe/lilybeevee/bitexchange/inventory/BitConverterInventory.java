package moe.lilybeevee.bitexchange.inventory;

import moe.lilybeevee.bitexchange.api.BitHelper;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

public interface BitConverterInventory extends ImplementedInventory {
    /**
     * Creates an inventory from the item list.
     */
    static BitConverterInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }

    /**
     * Creates a new inventory with the specified size.
     */
    static BitConverterInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    default ItemStack createStack(ItemStack targetStack, int count) {
        ItemStack storage = this.getStack(0);
        if (!storage.isEmpty() && !targetStack.isEmpty() && storage.getItem() instanceof BitStorageItem) {
            BitStorageItem item = (BitStorageItem) storage.getItem();
            Pair<ItemStack, Long> conversion = BitHelper.createStack(targetStack, item.getBits(storage), count);
            this.setStack(0, item.takeBits(storage, conversion.getRight()));
            return conversion.getLeft();
        }
        return ItemStack.EMPTY;
    }
}
