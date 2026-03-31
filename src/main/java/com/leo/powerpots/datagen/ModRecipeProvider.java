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
        shapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(0).get())
            .pattern("aaa")
            .pattern("bcb")
            .pattern("ded")
            .define('a', Items.GRAY_CONCRETE)
            .define('b', Items.RED_CONCRETE)
            .define('c', Items.FLOWER_POT)
            .define('d', Items.GREEN_CONCRETE)
            .define('e', Items.REDSTONE_BLOCK)
            .unlockedBy("has_flower_pot", has(Items.FLOWER_POT))
            .save(pWriter);

        // Tier 2
        shapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(1).get())
            .pattern("aaa")
            .pattern("bcb")
            .pattern("ded")
            .define('a', Items.GRAY_CONCRETE)
            .define('b', Items.RED_CONCRETE)
            .define('c', ModBlocks.POWER_BLOCKS.get(0).get())
            .define('d', Items.LAPIS_BLOCK)
            .define('e', Items.REDSTONE_BLOCK)
            .unlockedBy("has_power_pot_1", has(ModBlocks.POWER_BLOCKS.get(0).get()))
            .save(pWriter);

        // Tier 3
        shapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POWER_BLOCKS.get(2).get())
            .pattern("aaa")
            .pattern("bcb")
            .pattern("ded")
            .define('a', Items.GRAY_CONCRETE)
            .define('b', Items.RED_CONCRETE)
            .define('c', ModBlocks.POWER_BLOCKS.get(1).get())
            .define('d', Items.DIAMOND_BLOCK)
            .define('e', Items.REDSTONE_BLOCK)
            .unlockedBy("has_power_pot_2", has(ModBlocks.POWER_BLOCKS.get(1).get()))
            .save(pWriter);
    }

}
