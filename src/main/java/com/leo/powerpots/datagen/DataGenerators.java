package com.leo.powerpots.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(
        modid = "powerpots"
)
public class DataGenerators {
    public DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        // Client-side
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));

        // Server-side
        generator.addProvider(event.includeServer(), ModLootTableProvider.create(packOutput, provider));
        generator.addProvider(event.includeServer(), new ModBlockTagGenerator(packOutput, provider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, provider));
    }
}
