package moe.lilybeevee.bitexchange.screen.slot;

import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotInput extends Slot {
    private PlayerInventory playerInventory;

    public SlotInput(Inventory inventory, int index, int x, int y, PlayerInventory playerInventory) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        Item item = stack.getItem();
        return (item instanceof BitStorageItem) || (BitRegistry.get(item) > 0 && BitComponents.KNOWLEDGE.get(playerInventory.player).getLearned(item));
    }
}
