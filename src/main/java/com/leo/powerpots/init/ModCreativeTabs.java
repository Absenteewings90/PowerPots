package com.leo.powerpots.init;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS;
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS;

    public ModCreativeTabs() {
    }

    static {
        CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "powerpots");
        ITEMS = CREATIVE_MODE_TABS.register("items", () -> {
            CreativeModeTab.Builder var10000 = CreativeModeTab.builder().title(Component.translatable("powerpots.itemGroup.items"));
            Item var10001 = Items.STICK;
            Objects.requireNonNull(var10001);
            return var10000.icon(var10001::getDefaultInstance).displayItems((idp, output) -> {
                List<? extends Item> items = Moditems.ITEMS.getEntries().stream().map(DeferredHolder::get).toList();
                Objects.requireNonNull(output);
                items.forEach(output::accept);
                List<? extends Block> blocks = ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::get).toList();
                Objects.requireNonNull(output);
                blocks.forEach(output::accept);
            }).build();
        });
    }
}
