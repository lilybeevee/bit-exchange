package moe.lilybeevee.bitexchange.screen;

import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitInfo;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import moe.lilybeevee.bitexchange.client.gui.BitConverterScreen;
import moe.lilybeevee.bitexchange.inventory.BitConverterInventory;
import moe.lilybeevee.bitexchange.screen.slot.SlotInput;
import moe.lilybeevee.bitexchange.screen.slot.SlotConvert;
import moe.lilybeevee.bitexchange.screen.slot.SlotStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class BitConverterScreenHandler extends ScreenHandler {
    public static final int CONVERSION_SLOT = 2;
    public static final int PLAYER_SLOT = 34;
    private final BitConverterInventory inventory;
    private final PlayerEntity player;
    public final DefaultedList<ItemStack> itemList = DefaultedList.of();
    public final List<Item> knowledgeList;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public BitConverterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, BitConverterInventory.ofSize(2));
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public BitConverterScreenHandler(int syncId, PlayerInventory playerInventory, BitConverterInventory inventory) {
        super(BitExchange.BIT_CONVERTER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.player = playerInventory.player;
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(this.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;
        //Server-side bit converter slots
        this.addSlot(new SlotStorage(inventory, 0, 8, 104));
        this.addSlot(new SlotInput(inventory, 1, 152, 104, playerInventory));
        //Bit conversion inventory
        for (m = 0; m < 4; ++m) {
            for (l = 0; l < 8; ++l) {
                this.addSlot(new SlotConvert(BitConverterScreen.INVENTORY,l + m * 8, 8 + l * 18, 24 + m * 18, inventory));
            }
        }
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 130 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 188));
        }

        //Fill the inventory
        this.knowledgeList = BitComponents.KNOWLEDGE.get(this.player).getAllLearned();
        this.knowledgeList.sort((o1, o2) -> {
            long a = BitRegistry.get(o1);
            long b = BitRegistry.get(o2);
            return Long.compare(b, a);
        });
        this.buildList("", 0f);
    }

    public long getBits() {
        ItemStack stack = this.slots.get(0).getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof BitStorageItem) {
            return ((BitStorageItem) stack.getItem()).getBits(stack);
        }
        return -1;
    }

    public void buildList(String search, float scroll) {
        itemList.clear();
        long bits = getBits();
        for (Item item : knowledgeList) {
            long itemBits = BitRegistry.get(item);
            if (itemBits > 0) {
                String name = item.getName().getString().toLowerCase();
                if ((search.isEmpty() || name.contains(search.toLowerCase())) && bits >= itemBits) {
                    ItemStack stack =  item.getDefaultStack();
                    stack.setCount(1);
                    itemList.add(stack);
                }
            }
        }
        scrollItems(scroll);
    }

    public void scrollItems(float position) {
        int i = (this.itemList.size() + 8 - 1) / 8 - 4;
        int j = (int)((double)(position * (float)i) + 0.5D);
        if (j < 0) {
            j = 0;
        }

        for(int k = 0; k < 4; ++k) {
            for(int l = 0; l < 8; ++l) {
                int m = l + (k + j) * 8;
                if (m >= 0 && m < this.itemList.size()) {
                    this.slots.get(CONVERSION_SLOT + l + k * 8).setStack(this.itemList.get(m));
                } else {
                    this.slots.get(CONVERSION_SLOT + l + k * 8).setStack(ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            if (slot.inventory == BitConverterScreen.INVENTORY) {
                newStack = this.inventory.createStack(slot.getStack(), slot.getStack().getMaxCount());
                if (!this.insertItem(newStack, PLAYER_SLOT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                newStack = originalStack.copy();
                if (invSlot < PLAYER_SLOT) {
                    if (!this.insertItem(originalStack, PLAYER_SLOT, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                    return ItemStack.EMPTY;
                }
                if (originalStack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }
            }
        }

        return newStack;
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i >= CONVERSION_SLOT && i < PLAYER_SLOT) {
            Slot slot = this.slots.get(i);
            if (slot == null || !slot.canTakeItems(playerEntity)) {
                return ItemStack.EMPTY;
            }
            ItemStack resultStack = ItemStack.EMPTY;
            if (actionType == SlotActionType.QUICK_MOVE) {
                if (slot.getStack().isEmpty()) {
                    return ItemStack.EMPTY;
                }
                resultStack = slot.getStack().copy();
                ItemStack itemStack = this.inventory.createStack(slot.getStack(), slot.getStack().getMaxCount());
                while(!itemStack.isEmpty() && ItemStack.areItemsEqualIgnoreDamage(slot.getStack(), itemStack)) {
                    if (!this.insertItem(itemStack, PLAYER_SLOT, this.slots.size(), true)) {
                        break;
                    }
                }
            } else if (actionType == SlotActionType.PICKUP) {
                ItemStack itemStack = slot.getStack();
                PlayerInventory playerInventory = playerEntity.inventory;
                ItemStack cursorStack = playerInventory.getCursorStack();

                resultStack = itemStack.copy();

                if (cursorStack.isEmpty()) {
                    if (itemStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    ItemStack newStack = this.inventory.createStack(itemStack, j == 0 ? itemStack.getMaxCount() : 1);
                    if (!newStack.isEmpty()) {
                        playerInventory.setCursorStack(newStack);
                        slot.onTakeItem(playerEntity, newStack);
                    }
                } else if (j == 0 && this.slots.get(1).canInsert(cursorStack) && this.slots.get(1).getStack().isEmpty()) {
                    this.slots.get(1).setStack(cursorStack);
                    playerInventory.setCursorStack(ItemStack.EMPTY);
                } else if (cursorStack.getCount() < cursorStack.getMaxCount()) {
                    for (BitInfo bitInfo : BitRegistry.getAll()) {
                        ItemStack originalStack = bitInfo.item.getDefaultStack();
                        if (canStacksCombine(originalStack, cursorStack)) {
                            int count = cursorStack.getMaxCount() - cursorStack.getCount();
                            if (count > 0) {
                                ItemStack newStack = this.inventory.createStack(originalStack, j == 0 ? count : Math.min(count, 1));
                                if (!newStack.isEmpty()) {
                                    cursorStack.increment(newStack.getCount());
                                    slot.onTakeItem(playerEntity, newStack);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            return resultStack;
        }
        return super.onSlotClick(i, j, actionType, playerEntity);
    }
}
