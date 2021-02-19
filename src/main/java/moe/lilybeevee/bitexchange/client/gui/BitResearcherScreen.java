package moe.lilybeevee.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.lilybeevee.bitexchange.screen.BitConverterScreenHandler;
import moe.lilybeevee.bitexchange.screen.slot.SlotInput;
import moe.lilybeevee.bitexchange.screen.slot.SlotResearch;
import moe.lilybeevee.bitexchange.screen.slot.SlotStorage;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BitResearcherScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_researcher.png");

    public BitResearcherScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawRedSlots(matrices, x, y);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    private void drawRedSlots(MatrixStack matrices, int x, int y) {
        if (!client.player.inventory.getCursorStack().isEmpty()) {
            drawRedSlotsForStack(matrices, x, y, client.player.inventory.getCursorStack());
        } else if (focusedSlot != null) {
            if (focusedSlot.inventory == playerInventory && !focusedSlot.getStack().isEmpty()) {
                drawRedSlotsForStack(matrices, x, y, focusedSlot.getStack());
            } else if (focusedSlot == handler.slots.get(0)) {
                for (int i = 1; i < handler.slots.size(); i++) {
                    ItemStack stack = handler.slots.get(i).getStack();
                    if (!stack.isEmpty() && !handler.slots.get(0).canInsert(stack)) {
                        drawRedSlot(matrices, x, y, handler.slots.get(i));
                    }
                }
            }
        }
    }

    private void drawRedSlotsForStack(MatrixStack matrices, int x, int y, ItemStack stack) {
        if (!handler.slots.get(0).canInsert(stack)) {
            drawRedSlot(matrices, x, y, handler.slots.get(0));
        }
    }

    private void drawRedSlot(MatrixStack matrices, int x, int y, Slot slot) {
        if (slot instanceof SlotResearch) {
            drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, backgroundWidth + 16, 0, 18, 18);
        } else {
            drawTexture(matrices, x + slot.x, y + slot.y, backgroundWidth, 0, 16, 16);
        }
    }
}
