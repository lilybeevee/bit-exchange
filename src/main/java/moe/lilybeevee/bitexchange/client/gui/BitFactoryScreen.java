package moe.lilybeevee.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.lilybeevee.bitexchange.screen.BitConverterScreenHandler;
import moe.lilybeevee.bitexchange.screen.BitFactoryScreenHandler;
import moe.lilybeevee.bitexchange.screen.slot.SlotInput;
import moe.lilybeevee.bitexchange.screen.slot.SlotStorage;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BitFactoryScreen extends HandledScreen<ScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_factory.png");
    private Text bitText;
    private float progress;

    public BitFactoryScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void tick() {
        super.tick();
        updateBitText();
        updateProgress();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawRedSlots(matrices, x, y);
        drawTexture(matrices, x + 26, y + 17, 0, backgroundHeight, (int)Math.floor(88 * progress), 16);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        if (bitText != null) {
            textRenderer.draw(matrices, bitText, 61 - (textRenderer.getWidth(bitText) / 2), 38, 0xFFFFFF);
        }
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
        updateBitText();
        updateProgress();
    }

    private BitFactoryScreenHandler getBFHandler() {
        return (BitFactoryScreenHandler)getScreenHandler();
    }

    private void drawRedSlots(MatrixStack matrices, int x, int y) {
        if (!client.player.inventory.getCursorStack().isEmpty()) {
            drawRedSlotsForStack(matrices, x, y, client.player.inventory.getCursorStack());
        } else if (focusedSlot != null) {
            if (focusedSlot.inventory == playerInventory && !focusedSlot.getStack().isEmpty()) {
                drawRedSlotsForStack(matrices, x, y, focusedSlot.getStack());
            } else {
                for (int i = 0; i < 3; i++) {
                    if (focusedSlot == handler.slots.get(i)) {
                        int start = BitFactoryScreenHandler.PLAYER_SLOT;
                        for (int j = start; j < handler.slots.size(); j++) {
                            ItemStack stack = handler.slots.get(j).getStack();
                            if (!stack.isEmpty() && !handler.slots.get(i).canInsert(stack)) {
                                drawRedSlot(matrices, x, y, handler.slots.get(j));
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawRedSlotsForStack(MatrixStack matrices, int x, int y, ItemStack stack) {
        for (int i = 0; i < 3; i++) {
            if (!handler.slots.get(i).canInsert(stack)) {
                drawRedSlot(matrices, x, y, handler.slots.get(i));
            }
        }
    }

    private void drawRedSlot(MatrixStack matrices, int x, int y, Slot slot) {
        if (slot instanceof SlotInput) {
            drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, backgroundWidth + 32, 0, 18, 18);
        } else if (slot instanceof SlotStorage) {
            drawTexture(matrices, x + slot.x, y + slot.y , backgroundWidth + 16, 0, 16, 16);
        } else {
            drawTexture(matrices, x + slot.x, y + slot.y, backgroundWidth, 0, 16, 16);
        }
    }

    private void updateBitText() {
        long bits = getBFHandler().getBits();
        if (bits >= 0) {
            bitText = new LiteralText("Bits: " + bits).formatted(Formatting.DARK_PURPLE);
        } else {
            bitText = new LiteralText("Insert Bit Array").formatted(Formatting.RED);
        }
    }

    private void updateProgress() {
        long bits = getBFHandler().getBits();
        long maxBits = getBFHandler().getResourceBits();
        if (bits >= 0 && maxBits > 0) {
            progress = Math.min(1f, (float)bits / maxBits);
        } else {
            progress = 0f;
        }
    }
}
