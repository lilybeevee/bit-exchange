package moe.lilybeevee.bitexchange.block.entity;

import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitHelper;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import moe.lilybeevee.bitexchange.inventory.ImplementedInventory;
import moe.lilybeevee.bitexchange.screen.BitFactoryScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class BitFactoryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory, ImplementedInventory, Tickable {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);

    public BitFactoryBlockEntity() {
        super(BitExchange.BIT_FACTORY_BLOCK_ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitFactoryScreenHandler(syncId, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            ItemStack storage = this.inventory.get(0);
            ItemStack resource = this.inventory.get(1);
            ItemStack input = this.inventory.get(2);
            if (!storage.isEmpty()) {
                if (!(storage.getItem() instanceof BitStorageItem)) {
                    return;
                }
                BitStorageItem storageItem = (BitStorageItem)storage.getItem();

                if (!input.isEmpty()) {
                    Pair<ItemStack, Long> converted = BitHelper.convertToBits(input, storageItem.getMaxBits(storage) - storageItem.getBits(storage));

                    this.inventory.set(0, storageItem.addBits(storage, converted.getRight()));
                    this.inventory.set(2, converted.getLeft());
                    this.markDirty();
                }

                if (!resource.isEmpty()) {
                    Item item = resource.getItem();

                    if (BitRegistry.isResource(item) && storageItem.getBits(storage) >= BitRegistry.get(item) && createOutput(item)) {
                        this.inventory.set(0, storageItem.takeBits(storage, BitRegistry.get(item)));
                        this.markDirty();
                    }
                }
            }
        }
    }

    public boolean createOutput(Item item) {
        ItemStack stack = item.getDefaultStack();
        for (int i = 3; i < 12; i++) {
            ItemStack slot = this.inventory.get(i);
            if (slot.isEmpty()) {
                this.inventory.set(i, stack);
                return true;
            } else if (slot.getMaxCount() > slot.getCount() && canMergeItems(slot, stack)) {
                slot.increment(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 2 && BitRegistry.get(stack.getItem()) > 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot > 2;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areTagsEqual(first, second);
        }
    }
}
