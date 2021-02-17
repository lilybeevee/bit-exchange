package moe.lilybeevee.bitexchange.block.entity;

import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.component.BitKnowledgeComponent;
import moe.lilybeevee.bitexchange.inventory.ImplementedInventory;
import moe.lilybeevee.bitexchange.screen.BitResearcherScreenHandler;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BitResearcherBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory, ImplementedInventory, Tickable {
    public UUID owner;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public BitResearcherBlockEntity() {
        super(BitExchange.BIT_RESEARCHER_BLOCK_ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitResearcherScreenHandler(syncId, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.inventory);
        if (tag.contains("Owner")) {
            owner = tag.getUuid("Owner");
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.inventory);
        if (owner != null) {
            tag.putUuid("Owner", this.owner);
        }
        return tag;
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            PlayerEntity player = getOwner();
            if (player != null) {
                ItemStack input = inventory.get(0);
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    BitKnowledgeComponent component = BitComponents.KNOWLEDGE.get(player);
                    int knowledge = component.getKnowledge(item);
                    if (knowledge < BitRegistry.getResearch(item)) {
                        int count = component.addKnowledge(item, input.getCount());
                        input.decrement(count);
                        if (component.getLearned(item)) {
                            player.sendMessage(new LiteralText("Researched item: ").formatted(Formatting.LIGHT_PURPLE).append(item.getDefaultStack().toHoverableText()), false);
                        }
                        this.markDirty();
                    }
                }
            }
        }
    }

    private PlayerEntity getOwner() {
        return this.owner != null ? world.getPlayerByUuid(this.owner) : null;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return BitRegistry.getResearch(stack.getItem()) > 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        PlayerEntity player = getOwner();
        return player != null && BitComponents.KNOWLEDGE.get(player).getLearned(stack.getItem());
    }
}
