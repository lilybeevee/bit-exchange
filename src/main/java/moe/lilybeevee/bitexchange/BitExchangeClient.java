package moe.lilybeevee.bitexchange;

import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import moe.lilybeevee.bitexchange.client.gui.BitConverterScreen;
import moe.lilybeevee.bitexchange.client.gui.BitFactoryScreen;
import moe.lilybeevee.bitexchange.client.gui.BitMinerScreen;
import moe.lilybeevee.bitexchange.client.gui.BitResearcherScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class BitExchangeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BitExchange.BIT_CONVERTER_SCREEN_HANDLER, BitConverterScreen::new);
        ScreenRegistry.register(BitExchange.BIT_RESEARCHER_SCREEN_HANDLER, BitResearcherScreen::new);
        ScreenRegistry.register(BitExchange.BIT_FACTORY_SCREEN_HANDLER, BitFactoryScreen::new);
        ScreenRegistry.register(BitExchange.BIT_MINER_SCREEN_HANDLER, BitMinerScreen::new);


        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (MinecraftClient.getInstance() == null) {
                return;
            }
            Item item = stack.getItem();
            if (item instanceof BitStorageItem) {
                BitStorageItem bitStorage = (BitStorageItem)item;
                String countText = String.valueOf(bitStorage.getBits(stack));
                if (bitStorage.displayMaxBits(stack)) {
                    countText += " / " + bitStorage.getMaxBits(stack);
                }
                lines.add(new LiteralText("Bits: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(countText).formatted(Formatting.YELLOW)));
            } else if (BitRegistry.get(item) > 0) {
                Screen screen = MinecraftClient.getInstance().currentScreen;
                boolean bitScreen = (screen instanceof BitConverterScreen) ||
                                    (screen instanceof BitResearcherScreen) ||
                                    (screen instanceof BitFactoryScreen);
                int research = BitComponents.KNOWLEDGE.get(MinecraftClient.getInstance().player).getKnowledge(item);
                int maxResearch = BitRegistry.getResearch(item);
                MutableText text = new LiteralText("Bits: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(String.valueOf(BitRegistry.get(item))).formatted(Formatting.YELLOW));
                lines.add(text);
                if (Screen.hasShiftDown() || bitScreen) {
                    text.append(new LiteralText(" [" + research + "/" + maxResearch + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                    text.append(new LiteralText(" [R]").formatted(BitRegistry.isResource(item) ? Formatting.GREEN : Formatting.RED));
                    if (stack.getCount() > 1) {
                        lines.add(new LiteralText("Stack: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(String.valueOf(BitRegistry.get(item) * stack.getCount())).formatted(Formatting.YELLOW)));
                    }
                }
            }
        });
    }
}
