package com.leo.powerpots.init;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.screen.UpgradeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PowerPots.MODID);

    public static final RegistryObject<MenuType<UpgradeMenu>> UPGRADE_MENU =
            MENUS.register("upgrade_menu",
                    () -> IForgeMenuType.create(UpgradeMenu::new));
}