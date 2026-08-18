package com.leo.powerpots.datagen;

import com.leo.powerpots.init.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockStateProvider extends BlockStateProvider {
    private final ExistingFileHelper existingFileHelper;

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, "powerpots", exFileHelper);
        this.existingFileHelper = exFileHelper;
    }

    protected void registerStatesAndModels() {
        for(DeferredHolder<Block, Block> block : ModBlocks.POWER_BLOCKS) {
            this.simpleBlockWithItem(block);
        }

    }

    private void simpleBlockWithItem(DeferredHolder<Block, Block> block) {
        this.simpleBlockWithItem((Block)block.get(), new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("powerpots", "block/" + block.getId().getPath())));
    }
}
