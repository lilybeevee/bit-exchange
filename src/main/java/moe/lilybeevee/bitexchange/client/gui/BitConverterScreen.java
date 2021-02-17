package moe.lilybeevee.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.screen.BitConverterScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import java.util.Objects;

public class BitConverterScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_converter.png");
    public static final SimpleInventory INVENTORY = new SimpleInventory(32);
    private TextFieldWidget searchBox;
    private Text bitText;
    private float scrollAmount;
    private boolean scrolling;

    public BitConverterScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, LiteralText.EMPTY);
    }

    @Override
    protected void init() {
        backgroundWidth = 176;
        backgroundHeight = 212;
        super.init();
        scrollAmount = 0f;
        scrolling = false;

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        searchBox = new TextFieldWidget(client.textRenderer, x + 9, y + 9, 142, 9, new TranslatableText("itemGroup.search"));
        searchBox.setMaxLength(50);
        searchBox.setHasBorder(false);
        searchBox.setVisible(true);
        searchBox.setSelected(true);
        searchBox.setFocusUnlocked(false);
        searchBox.setEditableColor(0xFFFFFF);
        searchBox.setText("");
        this.children.add(searchBox);

        client.keyboard.setRepeatEvents(true);
    }

    @Override
    public void removed() {
        client.keyboard.setRepeatEvents(false);
    }

    @Override
    public void tick() {
        super.tick();
        getBCHandler().buildList(searchBox.getText(), scrollAmount);
        updateBitText();
        if (!this.shouldScroll() && this.scrollAmount > 0) {
            this.scrollAmount = 0;
            getBCHandler().scrollItems(0);
        }
        searchBox.tick();
    }

    public BitConverterScreenHandler getBCHandler() {
        return (BitConverterScreenHandler)this.handler;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        int k = (int)(71.0F * scrollAmount);
        drawTexture(matrices, x + 156, y + 8 + k, 176 + (shouldScroll() ? 0 : 12), 0, 12, 15);
        searchBox.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        searchBox.x = x + 9;
        searchBox.y = y + 9;
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (bitText != null) {
            textRenderer.draw(matrices, bitText, 29, 108, 0xFFFFFF);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        String string = this.searchBox.getText();
        if (this.searchBox.charTyped(chr, keyCode)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                scrollAmount = 0;
                getBCHandler().buildList(this.searchBox.getText(), scrollAmount);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = searchBox.getText();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, searchBox.getText())) {
                scrollAmount = 0;
                getBCHandler().buildList(this.searchBox.getText(), scrollAmount);
            }
            return true;
        } else {
            return searchBox.isFocused() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        if (shouldScroll() && mouseX >= x + 155 && mouseY >= y + 7 && mouseX < x + 169 && mouseY < y + 95) {
            scrolling = true;
        }
        if (searchBox.isMouseOver(mouseX, mouseY) && button == 1) {
            searchBox.setText("");
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (shouldScroll() && scrolling) {
            int i = y + 8;
            int j = i + 85;
            scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            scrollAmount = MathHelper.clamp(scrollAmount, 0.0F, 1.0F);
            getBCHandler().scrollItems(scrollAmount);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            BitExchange.log(Level.INFO, "Scroll: " + (amount / (double)i));
            this.scrollAmount = (float)((double)this.scrollAmount - amount / (double)i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            getBCHandler().scrollItems(scrollAmount);
        }

        return true;
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (slot != null && slot.inventory == INVENTORY && !searchBox.getText().isEmpty()) {
            searchBox.setSelectionStart(0);
            searchBox.setSelectionEnd(searchBox.getText().length());
        }
        super.onMouseClick(slot, invSlot, clickData, actionType);
    }

    public void updateBitText() {
        long bits = getBCHandler().getBits();
        if (bits >= 0) {
            bitText = new LiteralText("Bits: " + bits).formatted(Formatting.DARK_PURPLE);
        } else {
            bitText = new LiteralText("Insert Bit Array").formatted(Formatting.RED);
        }
    }

    public boolean shouldScroll() {
        return getBCHandler().itemList.size() > 32;
    }

    public int getMaxScroll() {
        return Math.max(1, (int)Math.ceil((getBCHandler().itemList.size() - 32) / (double)8));
    }
}
