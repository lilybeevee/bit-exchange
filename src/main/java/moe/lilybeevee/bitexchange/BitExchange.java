package moe.lilybeevee.bitexchange;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import moe.lilybeevee.bitexchange.api.BitInfo;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.block.BitConverterBlock;
import moe.lilybeevee.bitexchange.block.BitFactoryBlock;
import moe.lilybeevee.bitexchange.block.BitMinerBlock;
import moe.lilybeevee.bitexchange.block.BitResearcherBlock;
import moe.lilybeevee.bitexchange.block.entity.BitConverterBlockEntity;
import moe.lilybeevee.bitexchange.block.entity.BitFactoryBlockEntity;
import moe.lilybeevee.bitexchange.block.entity.BitMinerBlockEntity;
import moe.lilybeevee.bitexchange.block.entity.BitResearcherBlockEntity;
import moe.lilybeevee.bitexchange.client.gui.BitMinerScreen;
import moe.lilybeevee.bitexchange.item.BitArrayItem;
import moe.lilybeevee.bitexchange.registrybuilder.RecipeRegistryBuilder;
import moe.lilybeevee.bitexchange.registrybuilder.DataRegistryBuilder;
import moe.lilybeevee.bitexchange.screen.BitConverterScreenHandler;
import moe.lilybeevee.bitexchange.screen.BitFactoryScreenHandler;
import moe.lilybeevee.bitexchange.screen.BitMinerScreenHandler;
import moe.lilybeevee.bitexchange.screen.BitResearcherScreenHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

// literal("foo")
import static net.minecraft.server.command.CommandManager.literal;
// argument("bar", word())
import static net.minecraft.server.command.CommandManager.argument;

public class BitExchange implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "bitexchange";
    public static final String MOD_NAME = "Bit Exchange";

    public static final Item BIT_ARRAY_ITEM;
    public static final Item BIT_ITEM;
    public static final Item BYTE_ITEM;
    public static final Block BIT_CONVERTER_BLOCK;
    public static final Block BIT_RESEARCHER_BLOCK;
    public static final Block BIT_FACTORY_BLOCK;
    public static final Block BIT_MINER_BLOCK;
    public static final Block BYTE_MINER_BLOCK;
    public static final BlockItem BIT_CONVERTER_BLOCK_ITEM;
    public static final BlockItem BIT_RESEARCHER_BLOCK_ITEM;
    public static final BlockItem BIT_FACTORY_BLOCK_ITEM;
    public static final BlockItem BIT_MINER_BLOCK_ITEM;
    public static final BlockItem BYTE_MINER_BLOCK_ITEM;
    public static final BlockEntityType<BitConverterBlockEntity> BIT_CONVERTER_BLOCK_ENTITY;
    public static final BlockEntityType<BitResearcherBlockEntity> BIT_RESEARCHER_BLOCK_ENTITY;
    public static final BlockEntityType<BitFactoryBlockEntity> BIT_FACTORY_BLOCK_ENTITY;
    public static final BlockEntityType<BitMinerBlockEntity> BIT_MINER_BLOCK_ENTITY;
    public static final ScreenHandlerType<BitConverterScreenHandler> BIT_CONVERTER_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitResearcherScreenHandler> BIT_RESEARCHER_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitFactoryScreenHandler> BIT_FACTORY_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitMinerScreenHandler> BIT_MINER_SCREEN_HANDLER;

    static {
        BIT_ARRAY_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_array"), new BitArrayItem(new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        BYTE_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "byte"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));

        BIT_CONVERTER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_converter"), new BitConverterBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_RESEARCHER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_researcher"), new BitResearcherBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_FACTORY_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_factory"), new BitFactoryBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_miner"), new BitMinerBlock(BIT_ITEM, 20, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BYTE_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "byte_miner"), new BitMinerBlock(BYTE_ITEM, 20, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));

        BIT_CONVERTER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_converter"), new BlockItem(BIT_CONVERTER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_RESEARCHER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_researcher"), new BlockItem(BIT_RESEARCHER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_FACTORY_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_factory"), new BlockItem(BIT_FACTORY_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_miner"), new BlockItem(BIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BYTE_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "byte_miner"), new BlockItem(BYTE_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

        BIT_CONVERTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_converter"), BlockEntityType.Builder.create(BitConverterBlockEntity::new, BIT_CONVERTER_BLOCK).build(null));
        BIT_RESEARCHER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_researcher"), BlockEntityType.Builder.create(BitResearcherBlockEntity::new, BIT_RESEARCHER_BLOCK).build(null));
        BIT_FACTORY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_factory"), BlockEntityType.Builder.create(BitFactoryBlockEntity::new, BIT_FACTORY_BLOCK).build(null));
        BIT_MINER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_miner"), BlockEntityType.Builder.create(BitMinerBlockEntity::new, BIT_MINER_BLOCK, BYTE_MINER_BLOCK).build(null));

        BIT_CONVERTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "bit_converter"), BitConverterScreenHandler::new);
        BIT_RESEARCHER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "bit_researcher"), BitResearcherScreenHandler::new);
        BIT_FACTORY_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "bit_factory"), BitFactoryScreenHandler::new);
        BIT_MINER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "bit_miner"), BitMinerScreenHandler::new);

        BitRegistry.registerBuilder(new DataRegistryBuilder());
        BitRegistry.registerBuilder(new RecipeRegistryBuilder());
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MOD_ID, "bit_registry");
            }

            @Override
            public void apply(ResourceManager manager) {
                DataRegistryBuilder.loadResources(manager);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("bit")
                .then(literal("knowledge")
                    .then(literal("add")
                        .then(argument("item", ItemStackArgumentType.itemStack())
                            .executes((ctx) -> {
                                ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                BitComponents.KNOWLEDGE.get(ctx.getSource().getPlayer()).addKnowledge(itemArg.getItem(), Integer.MAX_VALUE);
                                return 1;
                            })
                        )
                    ).then(literal("set")
                        .then(argument("item", ItemStackArgumentType.itemStack())
                            .then(argument("count", IntegerArgumentType.integer(0))
                                .executes((ctx) -> {
                                    ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                    BitComponents.KNOWLEDGE.get(ctx.getSource().getPlayer()).addKnowledge(itemArg.getItem(), Integer.MAX_VALUE);
                                    return 1;
                                })
                            )
                        )
                    ).then(literal("complete")
                        .executes((ctx) -> {
                            Map<Item, Integer> knowledge = BitComponents.KNOWLEDGE.get(ctx.getSource().getPlayer()).getAllKnowledge();
                            for (BitInfo info : BitRegistry.getAll()) {
                                knowledge.put(info.item, info.research);
                            }
                            BitComponents.KNOWLEDGE.get(ctx.getSource().getPlayer()).setAllKnowledge(knowledge);
                            return 1;
                        })
                    ).then(literal("clear")
                        .executes((ctx) -> {
                            BitComponents.KNOWLEDGE.get(ctx.getSource().getPlayer()).setAllKnowledge(new HashMap<>());
                            return 1;
                        })
                    )
                )
            );
        });
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

    public static void error(String message, Object o) {
        LOGGER.error("["+MOD_NAME+"] " + message, o);
    }
}