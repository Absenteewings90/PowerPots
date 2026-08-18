package com.leo.powerpots;

import com.leo.powerpots.config.Config;
import com.leo.powerpots.init.*;
import com.leo.powerpots.network.OpenUpgradeGuiPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod("powerpots")
public class PowerPots {
    public static final String MODID = "powerpots";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PowerPots(IEventBus modEventBus, ModContainer modContainer) {
        Config.initialize();

        Moditems.ITEMS.register(modEventBus);
        ModUpgrades.SPEED_UPGRADE.getId();
        ModUpgrades.OUTPUT_UPGRADE.getId();
        ModUpgrades.ENERGY_UPGRADE.getId();
        ModUpgrades.FORTUNE_UPGRADE.getId();

        ModMenuTypes.MENUS.register(modEventBus);
        ModBlocks.register(modEventBus);
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
