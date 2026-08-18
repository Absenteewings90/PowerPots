package com.leo.powerpots.init;

import com.leo.powerpots.block.PotTier;
import com.leo.powerpots.config.Config;
import com.leo.powerpots.block.PowerPotBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS;
    public static List<DeferredHolder<Block, Block>> POWER_BLOCKS;

    public ModBlocks() {
    }

    public static Block[] getBlocks() {
        return (Block[])POWER_BLOCKS.stream().map(DeferredHolder::get).toArray((x$0) -> new Block[x$0]);
    }

    public static void register(IEventBus bus) {
        for(PotTier tier : Config.INSTANCE.TIERS) {
            if (tier.index() != 0) {
                POWER_BLOCKS.add(registerBlock("power_pot_" + tier.index(), () -> new PowerPotBlock(tier)));
            }
        }

        BLOCKS.register(bus);
    }

    public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        Moditems.ITEMS.register(name, () -> new BlockItem((Block)block.get(), new Item.Properties()));
    }

    static {
        BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, "powerpots");
        POWER_BLOCKS = new ArrayList<>();
    }
}
