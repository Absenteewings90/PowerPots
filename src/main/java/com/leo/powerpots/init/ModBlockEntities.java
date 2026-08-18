package com.leo.powerpots.init;

import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerPotBlockEntity>> POWER_POT_BE;

    public ModBlockEntities() {
    }

    static {
        BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "powerpots");
        POWER_POT_BE = BLOCK_ENTITIES.register("power_pot_be", () -> Builder.of(PowerPotBlockEntity::new, ModBlocks.getBlocks()).build(null));
    }
}

