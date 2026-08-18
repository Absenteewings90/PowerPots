package com.leo.powerpots.init;

import com.leo.powerpots.upgrade.PotUpgrade;
import com.leo.powerpots.upgrade.UpgradeItem;
import com.leo.powerpots.upgrade.UpgradeType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModUpgrades {

    public static final DeferredRegister<Item> ITEMS = Moditems.ITEMS;

    public static final DeferredHolder<Item, UpgradeItem> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.SPEED, 1.5f)));

    public static final DeferredHolder<Item, UpgradeItem> OUTPUT_UPGRADE = ITEMS.register("output_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.OUTPUT, 1.5f)));

    public static final DeferredHolder<Item, UpgradeItem> ENERGY_UPGRADE = ITEMS.register("energy_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.ENERGY, 0.75f)));

    public static final DeferredHolder<Item, UpgradeItem> FORTUNE_UPGRADE = ITEMS.register("fortune_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.FORTUNE, 1.0f)));
}