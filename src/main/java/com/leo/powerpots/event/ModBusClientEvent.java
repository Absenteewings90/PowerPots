package com.leo.powerpots.event;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.client.ModKeyBindings;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.init.ModMenuTypes;
import com.leo.powerpots.screen.UpgradeScreen;
import net.darkhax.botanypots.block.BotanyPotRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.leo.powerpots.init.ModBlocks;

@Mod.EventBusSubscriber(modid = PowerPots.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModBusClientEvent {

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.POWER_POT_BE.get(), BotanyPotRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.UPGRADE_MENU.get(), UpgradeScreen::new);
            for (Block block : ModBlocks.getBlocks()) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.onRegisterKeyMappings(event);
    }
}
