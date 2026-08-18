package com.leo.powerpots.event;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import com.leo.powerpots.client.ModKeyBindings;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.init.ModBlocks;
import com.leo.powerpots.init.ModMenuTypes;
import com.leo.powerpots.network.OpenUpgradeGuiPacket;
import com.leo.powerpots.screen.UpgradeScreen;
import net.darkhax.botanypots.common.impl.block.BotanyPotRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = PowerPots.MODID, value = Dist.CLIENT)
public class ModBusClientEvent {

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.POWER_POT_BE.get(), BotanyPotRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UPGRADE_MENU.get(), UpgradeScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Block block : ModBlocks.getBlocks()) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.OPEN_UPGRADE_GUI);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (ModKeyBindings.OPEN_UPGRADE_GUI.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.hitResult instanceof BlockHitResult hit) {
                BlockPos pos = hit.getBlockPos();
                if (mc.level.getBlockEntity(pos) instanceof PowerPotBlockEntity) {
                    // send packet to server to open the GUI
                    PacketDistributor.sendToServer(new OpenUpgradeGuiPacket(pos));
                }
            }
        }
    }
}
