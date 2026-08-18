package com.leo.powerpots.upgrade;

public class PotUpgrade {
    private final UpgradeType type;
    private final float modifier; // e.g. 1.5 = 50% bonus per upgrade

    public PotUpgrade(UpgradeType type, float modifier) {
        this.type = type;
        this.modifier = modifier;
    }

    public UpgradeType getType() {
        return type;
    }

    public float getModifier() {
        return modifier;
    }
}
