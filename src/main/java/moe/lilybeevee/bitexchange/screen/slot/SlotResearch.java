package moe.lilybeevee.bitexchange.screen.slot;

import moe.lilybeevee.bitexchange.api.BitRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotResearch extends Slot {
    public SlotResearch(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return BitRegistry.getResearch(stack.getItem()) > 0;
    }
}
