package com.leo.powerpots.event;

import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

@EventBusSubscriber(modid = "powerpots")
public class ModBusEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                EnergyStorage.BLOCK,
                ModBlockEntities.POWER_POT_BE.get(),
                (be, side) -> ((PowerPotBlockEntity) be).getEnergyStorage()
        );

        event.registerBlockEntity(
                ItemHandler.BLOCK,
                ModBlockEntities.POWER_POT_BE.get(),
                (be, side) -> {
                    if (be instanceof PowerPotBlockEntity pot) {
                        return side == Direction.DOWN ? new SidedInvWrapper(pot, Direction.DOWN) : null;
                    }
                    return null;
                }
        );
    }
}
