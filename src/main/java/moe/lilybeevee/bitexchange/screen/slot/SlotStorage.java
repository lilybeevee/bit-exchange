package moe.lilybeevee.bitexchange.screen.slot;

import moe.lilybeevee.bitexchange.api.BitStorageItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotStorage extends Slot {
    public SlotStorage(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof BitStorageItem;
    }
}
