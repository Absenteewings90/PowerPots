package com.leo.powerpots.block;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.entity.PowerPotBE;
import com.leo.powerpots.config.Config;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.screen.UpgradeMenu;
import com.leo.powerpots.upgrade.UpgradeItem;
import net.darkhax.botanypots.block.BlockBotanyPot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerPotBlock extends BlockBotanyPot {

    PotTier tier;

    public PowerPotBlock(PotTier tier) {
        super(true);
        this.tier = tier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return blockEntity(pPos, pState, null, tier);
    }

    public PowerPotBE blockEntity(BlockPos pos, BlockState state, @Nullable BlockGetter level, PotTier tier) {
        if(level == null) return new PowerPotBE(pos, state, tier);
        if(level.getBlockEntity(pos) instanceof PowerPotBE be) return be;

        return new PowerPotBE(pos, state, tier);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        String path = ForgeRegistries.ITEMS.getKey(pStack.getItem()).getPath();
        char c = path.charAt(path.length() - 1);
        int i = Integer.parseInt(String.valueOf(c));
        PotTier tier = Config.INSTANCE.TIERS.get(i - 1);

        pTooltip.add(Component.translatable("tooltip." + PowerPots.MODID + ".energy", tier.powerEachTick()));
        pTooltip.add(Component.translatable("tooltip." + PowerPots.MODID + ".speed", tier.speedModifier()));
        pTooltip.add(Component.translatable("tooltip." + PowerPots.MODID + ".item", tier.itemAmountMultiplier()));
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.POWER_POT_BE.get(), PowerPotBE::tickPot);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos,
                              BlockState state, @Nullable BlockEntity be,
                              ItemStack tool) {
        if (!level.isClientSide && be instanceof PowerPotBE powerPotBE) {
            CompoundTag upgradeTag = new CompoundTag();
            upgradeTag.putInt("potTier", powerPotBE.getTier().index());

            boolean hasUpgrades = false;
            for (int i = 0; i < 3; i++) {
                ItemStack upgrade = powerPotBE.getUpgradeSlot(i);
                if (!upgrade.isEmpty()) {
                    CompoundTag slotTag = new CompoundTag();
                    upgrade.save(slotTag);
                    upgradeTag.put("upgrade_" + i, slotTag);
                    hasUpgrades = true;
                }
            }

            if (hasUpgrades) {
                powerPotBE.skipDrop = true;

                ItemStack drop = new ItemStack(this);
                drop.addTagElement("BlockEntityTag", upgradeTag);
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
            if (level.getBlockEntity(pos) instanceof PowerPotBE be && be.skipDrop) {
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

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PowerPotBE be) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                for (int i = 0; i < 3; i++) {
                    if (tag.contains("upgrade_" + i)) {
                        be.setUpgradeSlot(i, ItemStack.of(tag.getCompound("upgrade_" + i)));
                    }
                }
                be.setChanged();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PowerPotBE) {
                final PowerPotBE powerPotBE = (PowerPotBE) be;

                // shift + right click opens upgrade GUI
                if (player.isCrouching()) {
                    NetworkHooks.openScreen(
                            (ServerPlayer) player,
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new UpgradeMenu(id, inv, powerPotBE), // ← powerPotBE instance not class
                                    Component.translatable("screen.powerpots.upgrades")
                            ),
                            buf -> buf.writeBlockPos(pos)
                    );
                    return InteractionResult.CONSUME;
                }
            }
        }
        // normal right click opens BotanyPots GUI as usual
        return super.use(state, level, pos, player, hand, hit);
    }
}
