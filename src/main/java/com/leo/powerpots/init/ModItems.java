package com.leo.powerpots.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Moditems {
    public static final DeferredRegister<Item> ITEMS;

    public Moditems() {
    }

    static {
        ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "powerpots");
    }
}
