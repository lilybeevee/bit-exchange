package moe.lilybeevee.bitexchange.screen.slot;

import moe.lilybeevee.bitexchange.inventory.BitConverterInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotConvert extends Slot {
    private BitConverterInventory bcInventory;

    public SlotConvert(Inventory inventory, int index, int x, int y, BitConverterInventory bcInventory) {
        super(inventory, index, x, y);
        this.bcInventory = bcInventory;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return this.bcInventory.createStack(this.getStack(), amount);
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        return super.onTakeItem(player, stack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
