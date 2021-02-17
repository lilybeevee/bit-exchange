package moe.lilybeevee.bitexchange.block.entity;

import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitHelper;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import moe.lilybeevee.bitexchange.inventory.BitConverterInventory;
import moe.lilybeevee.bitexchange.screen.BitConverterScreenHandler;
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

public class BitConverterBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, BitConverterInventory, SidedInventory, Tickable {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public BitConverterBlockEntity() {
        super(BitExchange.BIT_CONVERTER_BLOCK_ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitConverterScreenHandler(syncId, inv, this);
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
    public boolean isValid(int slot, ItemStack stack) {
        Item item = stack.getItem();
        if (slot == 0) {
            return (item instanceof BitStorageItem);
        } else {
            return (item instanceof BitStorageItem) || BitRegistry.get(item) > 0;
        }
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            ItemStack input = this.inventory.get(1);
            if (!input.isEmpty()) {
                this.inventory.set(1, consumeInput(input));
            }
        }
    }

    public ItemStack consumeInput(ItemStack input) {
        if (!world.isClient) {
            ItemStack storage = this.inventory.get(0);
            if (!storage.isEmpty() && !input.isEmpty()) {
                if (!(storage.getItem() instanceof BitStorageItem)) {
                    return input;
                }
                BitStorageItem storageItem = (BitStorageItem)storage.getItem();

                Pair<ItemStack, Long> converted = BitHelper.convertToBits(input, storageItem.getMaxBits(storage) - storageItem.getBits(storage));

                this.inventory.set(0, storageItem.addBits(storage, converted.getRight()));
                return converted.getLeft();
            }
        }
        return input;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }
}
