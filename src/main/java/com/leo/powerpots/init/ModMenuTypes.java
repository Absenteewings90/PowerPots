package com.leo.powerpots.init;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.screen.UpgradeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, PowerPots.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<UpgradeMenu>> UPGRADE_MENU =
            MENUS.register("upgrade_menu",
                    () -> IMenuTypeExtension.create(UpgradeMenu::new));
}