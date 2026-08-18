package com.leo.powerpots.upgrade;

import net.minecraft.world.item.Item;

public class UpgradeItem extends Item {
    private final PotUpgrade upgrade;

    public UpgradeItem(PotUpgrade upgrade) {
        super(new Item.Properties().stacksTo(1));
        this.upgrade = upgrade;
    }

    public PotUpgrade getUpgrade() {
        return upgrade;
    }
}
