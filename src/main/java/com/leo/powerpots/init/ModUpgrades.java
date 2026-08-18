package com.leo.powerpots.init;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.upgrade.PotUpgrade;
import com.leo.powerpots.upgrade.UpgradeItem;
import com.leo.powerpots.upgrade.UpgradeType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgrades {

    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;

    // each upgrade gives +50% per slot
    public static final RegistryObject<UpgradeItem> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.SPEED, 1.5f)));

    public static final RegistryObject<UpgradeItem> OUTPUT_UPGRADE = ITEMS.register("output_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.OUTPUT, 1.5f)));

    public static final RegistryObject<UpgradeItem> ENERGY_UPGRADE = ITEMS.register("energy_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.ENERGY, 0.75f))); // 0.75 = 25% cheaper per upgrade

    public static final RegistryObject<UpgradeItem> FORTUNE_UPGRADE = ITEMS.register("fortune_upgrade",
            () -> new UpgradeItem(new PotUpgrade(UpgradeType.FORTUNE, 1.0f))); // +1 fortune level per upgrade
}