package com.github.rimevel.waterways.common;

import com.github.rimevel.waterways.Waterways;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Register {
    public static class BLOCKS {
        public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(Registries.BLOCK, Waterways.MODID);

        private static BlockBehaviour.Properties properties(Block block)
        {
            return BlockBehaviour.Properties.copy(block);
        }

        private static class Add {
            public static RegistryObject<Block> Aqueduct(String id, Block block) {
                return REGISTER.register(id, () -> new AqueductBlock(properties(block)));
            }
        }

        public static final RegistryObject<Block> AQUEDUCT_STONE_BRICKS = Add.Aqueduct("aqueduct/stone_bricks", Blocks.STONE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_MOSSY_STONE_BRICKS = Add.Aqueduct("aqueduct/mossy_stone_bricks", Blocks.MOSSY_STONE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_DEEPSLATE_BRICKS = Add.Aqueduct("aqueduct/deepslate_bricks", Blocks.DEEPSLATE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_BRICKS = Add.Aqueduct("aqueduct/bricks", Blocks.BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_MUD_BRICKS = Add.Aqueduct("aqueduct/mud_bricks", Blocks.MUD_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_PRISMARINE_BRICKS = Add.Aqueduct("aqueduct/prismarine_bricks", Blocks.PRISMARINE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_NETHER_BRICKS = Add.Aqueduct("aqueduct/nether_bricks", Blocks.NETHER_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_RED_NETHER_BRICKS = Add.Aqueduct("aqueduct/red_nether_bricks", Blocks.RED_NETHER_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_POLISHED_BLACKSTONE_BRICKS = Add.Aqueduct("aqueduct/polished_blackstone_bricks", Blocks.POLISHED_BLACKSTONE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_END_STONE_BRICKS = Add.Aqueduct("aqueduct/end_stone_bricks", Blocks.END_STONE_BRICKS);
        public static final RegistryObject<Block> AQUEDUCT_QUARTZ_BRICKS = Add.Aqueduct("aqueduct/quartz_bricks", Blocks.QUARTZ_BRICKS);
    }

    public static class ITEMS {
        public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Waterways.MODID);

        private static class Add {
            public static RegistryObject<Item> BlockItem(String id, RegistryObject<Block> block) {
                return REGISTER.register(id, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(64)));
            }
        }

        public static final RegistryObject<Item> AQUEDUCT_STONE_BRICKS = Add.BlockItem("aqueduct/stone_bricks", BLOCKS.AQUEDUCT_STONE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_MOSSY_STONE_BRICKS = Add.BlockItem("aqueduct/mossy_stone_bricks", BLOCKS.AQUEDUCT_MOSSY_STONE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_DEEPSLATE_BRICKS = Add.BlockItem("aqueduct/deepslate_bricks", BLOCKS.AQUEDUCT_DEEPSLATE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_BRICKS = Add.BlockItem("aqueduct/bricks", BLOCKS.AQUEDUCT_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_MUD_BRICKS = Add.BlockItem("aqueduct/mud_bricks", BLOCKS.AQUEDUCT_MUD_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_PRISMARINE_BRICKS = Add.BlockItem("aqueduct/prismarine_bricks", BLOCKS.AQUEDUCT_PRISMARINE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_NETHER_BRICKS = Add.BlockItem("aqueduct/nether_bricks", BLOCKS.AQUEDUCT_NETHER_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_RED_NETHER_BRICKS = Add.BlockItem("aqueduct/red_nether_bricks", BLOCKS.AQUEDUCT_RED_NETHER_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_POLISHED_BLACKSTONE_BRICKS = Add.BlockItem("aqueduct/polished_blackstone_bricks", BLOCKS.AQUEDUCT_POLISHED_BLACKSTONE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_END_STONE_BRICKS = Add.BlockItem("aqueduct/end_stone_bricks", BLOCKS.AQUEDUCT_END_STONE_BRICKS);
        public static final RegistryObject<Item> AQUEDUCT_QUARTZ_BRICKS = Add.BlockItem("aqueduct/quartz_bricks", BLOCKS.AQUEDUCT_QUARTZ_BRICKS);
    }

    public static class CREATIVE_TABS {
        public static DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Waterways.MODID);

        public static final RegistryObject<CreativeModeTab> TAB_WATERWAYS = REGISTER.register("waterways",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemgroup."+ Waterways.MODID + ".creativetab"))
                        .icon(() -> new ItemStack(ITEMS.AQUEDUCT_BRICKS.get()))
                        .displayItems((displayParams, output) -> {
                            ITEMS.REGISTER.getEntries().forEach(Item -> output.accept(Item.get()));
                        })
                        .build()
        );
    }

    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.REGISTER.register(bus);
        ITEMS.REGISTER.register(bus);
        CREATIVE_TABS.REGISTER.register(bus);
    }
}
