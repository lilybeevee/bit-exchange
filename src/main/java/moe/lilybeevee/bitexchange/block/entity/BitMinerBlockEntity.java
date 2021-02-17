package moe.lilybeevee.bitexchange.block.entity;

import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.component.BitKnowledgeComponent;
import moe.lilybeevee.bitexchange.block.BitMinerBlock;
import moe.lilybeevee.bitexchange.inventory.ImplementedInventory;
import moe.lilybeevee.bitexchange.screen.BitMinerScreenHandler;
import moe.lilybeevee.bitexchange.screen.BitResearcherScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BitMinerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, SidedInventory, ImplementedInventory, Tickable {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int miningProgress;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return miningProgress;
        }

        @Override
        public void set(int index, int value) {
            miningProgress = value;
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public BitMinerBlockEntity() {
        super(BitExchange.BIT_MINER_BLOCK_ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitMinerScreenHandler(syncId, inv, this, propertyDelegate);
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
            ItemStack stack = this.inventory.get(0);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxCount()) {
                miningProgress++;
                if (miningProgress >= getMiningSpeed()) {
                    if (stack.isEmpty()) {
                        this.inventory.set(0, getMiningOutput().getDefaultStack());
                    } else {
                        stack.increment(1);
                        this.inventory.set(0, stack);
                    }
                    this.markDirty();
                    miningProgress = 0;
                }
            } else {
                miningProgress = 0;
            }
        }
    }

    public int getMiningSpeed() {
        return ((BitMinerBlock)getCachedState().getBlock()).speed;
    }

    public Item getMiningOutput() {
        return ((BitMinerBlock)getCachedState().getBlock()).output;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(getMiningSpeed());
    }
}
