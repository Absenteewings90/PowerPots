package com.leo.powerpots.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {

        // Tier 1
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(0).get())
                    .pattern("bbb")
                    .pattern("rcr")
                    .pattern("dad")
                    .define('b', Items.GRAY_CONCRETE)
                    .define('r', Items.RED_CONCRETE)
                    .define('c', Items.FLOWER_POT)
                    .define('d', Items.GREEN_CONCRETE)
                    .define('a', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_flower_pot", has(Items.FLOWER_POT))
                    .save(pWriter);

            // Tier 2 — adjust ingredients as needed
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(1).get())
                    .pattern("bbb")
                    .pattern("rcr")
                    .pattern("dad")
                    .define('b', Items.GRAY_CONCRETE)
                    .define('r', Items.RED_CONCRETE)
                    .define('c', ModBlocks.POWER_BLOCKS.get(0).get()) // tier 1 pot as ingredient
                    .define('d', Items.LAPIS_BLOCK)
                    .define('a', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_power_pot_1", has(ModBlocks.POWER_BLOCKS.get(0).get()))
                    .save(pWriter);

            // Tier 3 — adjust ingredients as needed
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(2).get())
                    .pattern("bbb")
                    .pattern("rcr")
                    .pattern("dad")
                    .define('b', Items.GRAY_CONCRETE)
                    .define('r', Items.RED_CONCRETE)
                    .define('c', ModBlocks.POWER_BLOCKS.get(1).get()) // tier 2 pot as ingredient
                    .define('d', Items.DIAMOND_BLOCK)
                    .define('a', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_power_pot_2", has(ModBlocks.POWER_BLOCKS.get(1).get()))
                    .save(pWriter);

            // Tier 4 — adjust ingredients as needed
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(3).get())
                    .pattern("bbb")
                    .pattern("rcr")
                    .pattern("dad")
                    .define('b', Items.GRAY_CONCRETE)
                    .define('r', Items.RED_CONCRETE)
                    .define('c', ModBlocks.POWER_BLOCKS.get(2).get()) // tier 3 pot as ingredient
                    .define('d', Items.NETHERITE_BLOCK)
                    .define('a', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_power_pot_3", has(ModBlocks.POWER_BLOCKS.get(2).get()))
                    .save(pWriter);

        // Tier 5 — adjust ingredients as needed
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(4).get())
                .pattern("bbb")
                .pattern("rcr")
                .pattern("dad")
                .define('b', Items.GRAY_CONCRETE)
                .define('r', Items.RED_CONCRETE)
                .define('c', ModBlocks.POWER_BLOCKS.get(3).get()) // tier 3 pot as ingredient
                .define('d', Items.NETHERITE_BLOCK)
                .define('a', Items.DRAGON_EGG)
                .unlockedBy("has_power_pot_4", has(ModBlocks.POWER_BLOCKS.get(3).get()))
                .save(pWriter);

        // Speed Upgrade
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModUpgrades.SPEED_UPGRADE.get())
                .pattern(" S ")
                .pattern("SCS")
                .pattern(" S ")
                .define('S', Items.SUGAR)
                .define('C', Items.CLOCK)
                .unlockedBy("has_clock", has(Items.CLOCK))
                .save(pWriter);

        // Output Upgrade
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModUpgrades.OUTPUT_UPGRADE.get())
                .pattern(" F ")
                .pattern("FDF")
                .pattern(" F ")
                .define('F', Items.FERMENTED_SPIDER_EYE)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter);

        // Energy Upgrade
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModUpgrades.ENERGY_UPGRADE.get())
                .pattern(" R ")
                .pattern("RGR")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('G', Items.GLOWSTONE_DUST)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(pWriter);

        // Fortune Upgrade
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModUpgrades.FORTUNE_UPGRADE.get())
                .pattern(" E ")
                .pattern("ELE")
                .pattern(" E ")
                .define('E', Items.EMERALD)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(pWriter);
    }
}
