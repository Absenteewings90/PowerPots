package com.leo.powerpots.event;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.PowerPotBlock;
import com.leo.powerpots.block.entity.PowerPotBE;
import com.leo.powerpots.client.ModKeyBindings;
import com.leo.powerpots.network.ModNetworking;
import com.leo.powerpots.screen.UpgradeMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = PowerPots.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientForgeEvent {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.screen != null) return;

        if (ModKeyBindings.OPEN_UPGRADES.consumeClick()) {
            // check if player is looking at a power pot
            if (mc.hitResult instanceof BlockHitResult blockHit) {
                BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
                if (be instanceof PowerPotBE powerPotBE) {
                    // send request to server to open the GUI
                    ModNetworking.sendOpenUpgradeGui(blockHit.getBlockPos());
                }
            }
        }
    }
}
