package moe.lilybeevee.bitexchange.screen.slot;

import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotResource extends Slot {
    private final PlayerInventory playerInventory;

    public SlotResource(Inventory inventory, int index, int x, int y, PlayerInventory playerInventory) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        Item item = stack.getItem();
        return getStack().isEmpty() && BitRegistry.isResource(item) && BitComponents.KNOWLEDGE.get(playerInventory.player).getLearned(item);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
