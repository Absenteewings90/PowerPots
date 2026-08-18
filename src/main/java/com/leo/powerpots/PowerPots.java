package com.leo.powerpots;

import com.leo.powerpots.config.Config;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.init.ModBlocks;
import com.leo.powerpots.init.ModCreativeTabs;
import com.leo.powerpots.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PowerPots.MODID)
public class PowerPots {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "powerpots";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    public PowerPots() {
        Config.initialize();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModUpgrades.SPEED_UPGRADE.getId();
        ModUpgrades.OUTPUT_UPGRADE.getId();
        ModUpgrades.ENERGY_UPGRADE.getId();
        ModUpgrades.FORTUNE_UPGRADE.getId();

        ModBlocks.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                OpenUpgradeGuiPacket.TYPE,
                OpenUpgradeGuiPacket.STREAM_CODEC,
                OpenUpgradeGuiPacket::handle
        );
    }

    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath("powerpots", path);
    }
}
