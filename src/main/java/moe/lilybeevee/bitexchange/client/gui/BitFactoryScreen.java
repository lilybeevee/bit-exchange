package moe.lilybeevee.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.lilybeevee.bitexchange.screen.BitFactoryScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
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
        drawTexture(matrices, x + 26, y + 17, 1, backgroundHeight + 1, (int)Math.floor(88 * progress), 16);
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
