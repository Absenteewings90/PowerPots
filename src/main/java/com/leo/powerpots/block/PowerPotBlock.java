package com.leo.powerpots.block;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.config.Config;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import com.leo.powerpots.screen.UpgradeMenu;
import com.leo.powerpots.upgrade.UpgradeType;
import net.darkhax.botanypots.common.api.context.BlockEntityContext;
import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.darkhax.botanypots.common.impl.block.PotType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerPotBlock extends BotanyPotBlock {

    private final PotTier tier;

    public PowerPotBlock(PotTier tier) {
        super(MapColor.COLOR_GRAY, PotType.HOPPER);
        this.tier = tier;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerPotBlockEntity(pos, state, this.tier);
    }

    @Override
    public float getGrowthModifier(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        return 0.0f;
    }

    @Override
    public float getYieldModifier(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        if (context instanceof BlockEntityContext ctx && ctx.pot() instanceof PowerPotBlockEntity pot) {
            return pot.tier.itemAmountMultiplier() * pot.getUpgradeModifier(UpgradeType.OUTPUT);
        }
        return 1.0f;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        char c = path.charAt(path.length() - 1);
        int i = Integer.parseInt(String.valueOf(c));
        PotTier t = (PotTier) Config.INSTANCE.TIERS.get(i - 1);
        tooltip.add(Component.translatable("tooltip.powerpots.energy", t.powerEachTick()));
        tooltip.add(Component.translatable("tooltip.powerpots.speed", t.speedModifier()));
        tooltip.add(Component.translatable("tooltip.powerpots.item", t.itemAmountMultiplier()));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.POWER_POT_BE.get(), PowerPotBlockEntity::tickPot);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        if (level instanceof Level l && l.getBlockEntity(pos) instanceof PowerPotBlockEntity be) {
            CompoundTag tag = new CompoundTag();
            boolean hasData = false;

            for (int i = 0; i < 3; i++) {
                ItemStack upgrade = be.getUpgradeSlot(i);
                if (!upgrade.isEmpty()) {
                    tag.put("upgrade_" + i, (CompoundTag) upgrade.save(l.registryAccess()));
                    hasData = true;
                }
            }

            if (hasData) {
                tag.putString("id", "powerpots:power_pot_be");
                tag.putInt("potTier", be.tier.index());
                stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                        net.minecraft.world.item.component.CustomData.of(tag));
            }
        }
        return stack;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos,
                              BlockState state, @Nullable BlockEntity be,
                              ItemStack tool) {
        if (!level.isClientSide && be instanceof PowerPotBlockEntity powerPotBE) {
            CompoundTag upgradeTag = new CompoundTag();
            upgradeTag.putInt("potTier", powerPotBE.tier.index());

            boolean hasUpgrades = false;
            for (int i = 0; i < 3; i++) {
                ItemStack upgrade = powerPotBE.getUpgradeSlot(i);
                if (!upgrade.isEmpty()) {
                    // use save() which returns the tag instead of writing into one
                    CompoundTag slotTag = (CompoundTag) upgrade.save(level.registryAccess());
                    upgradeTag.put("upgrade_" + i, slotTag);
                    upgradeTag.putString("id", "powerpots:power_pot_be");
                    hasUpgrades = true;
                }
            }

            if (hasUpgrades) {
                powerPotBE.skipDrop = true;
                ItemStack drop = new ItemStack(this);
                drop.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                        net.minecraft.world.item.component.CustomData.of(upgradeTag));
                popResource(level, pos, drop);
                player.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(this));
                player.causeFoodExhaustion(0.005F);
                return;
            }
        }
        super.playerDestroy(level, player, pos, state, be, tool);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PowerPotBlockEntity be && be.skipDrop) {
                be.skipDrop = false;
                level.removeBlockEntity(pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PowerPotBlockEntity be) {
            net.minecraft.world.item.component.CustomData customData =
                    stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                PowerPots.LOGGER.info("setPlacedBy tag contents: {}", tag);
                for (int i = 0; i < 3; i++) {
                    if (tag.contains("upgrade_" + i)) {
                        be.setUpgradeSlot(i, ItemStack.parseOptional(
                                level.registryAccess(), tag.getCompound("upgrade_" + i)));
                    }
                }
                be.setChanged();
            } else {
                PowerPots.LOGGER.info("setPlacedBy: no CustomData found on stack");
            }
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PowerPotBlockEntity powerPotBE) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new UpgradeMenu(id, inv, powerPotBE),
                                    Component.translatable("screen.powerpots.upgrades")
                            ),
                            buf -> buf.writeBlockPos(pos)
                    );
                }
            }
            return ItemInteractionResult.CONSUME;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
