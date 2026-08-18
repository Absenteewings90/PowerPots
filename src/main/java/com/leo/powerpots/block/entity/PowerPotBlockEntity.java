package com.leo.powerpots.block.entity;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.PotTier;
import com.leo.powerpots.config.Config;
import com.leo.powerpots.energy.ModEnergyStorage;
import com.leo.powerpots.upgrade.PotUpgrade;
import com.leo.powerpots.upgrade.UpgradeItem;
import com.leo.powerpots.upgrade.UpgradeType;
import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class PowerPotBlockEntity extends BotanyPotBlockEntity {

    public PotTier tier;
    public ModEnergyStorage energyStorage;
    private final ItemStack[] upgradeSlots = new ItemStack[3];
    public boolean skipDrop = false;
    public PotTier getTier() {
        return tier;
    }

    public PowerPotBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, PotTier.ZERO);
    }

    public PowerPotBlockEntity(BlockPos pos, BlockState state, PotTier tier) {
        super(getBEType(), pos, state);
        this.tier = tier;
        this.energyStorage = new ModEnergyStorage(tier.powerStorage(), tier.powerStorage(), 0, 0);
        for (int i = 0; i < upgradeSlots.length; i++) {
            upgradeSlots[i] = ItemStack.EMPTY;
        }
    }

    // ── BE type ───────────────────────────────────────────────────────────────

    public static CachedSupplier<BlockEntityType<BotanyPotBlockEntity>> getBEType() {
        return CachedSupplier.of(BuiltInRegistries.BLOCK_ENTITY_TYPE, PowerPots.modLoc("power_pot_be")).cast();
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void tickPot(Level level, BlockPos pos, BlockState state, PowerPotBlockEntity pot) {
        if (pot.isRemoved() || pot.level == null) return;
        
        if (pot.energyStorage.getEnergyStored() < pot.tier.powerEachTick()) {
            return;
        }
        
        if (!level.isClientSide) {
            int energyCost = Math.min(
                    (int)(pot.tier.powerEachTick() * pot.getUpgradeModifier(UpgradeType.ENERGY)),
                    pot.energyStorage.getEnergyStored()
            );
            pot.energyStorage.removeEnergy(energyCost);
        }
        
        Soil soil = pot.getOrInvalidateSoil();
        Crop crop = pot.getOrInvalidateCrop();

        if (soil != null) soil.onTick(pot.getRecipeContext(), level);
        if (crop != null) crop.onTick(pot.getRecipeContext(), level);

        if (soil != null && crop != null && crop.isGrowthSustained(pot.getRecipeContext(), level)) {
            if (pot.growCooldown.getTicks() > 0.0F) {
                pot.growCooldown.tickDown(level);
            }

            if (pot.growCooldown.getTicks() <= 0.0F) {
                float speedMult = pot.tier.speedModifier() * pot.getUpgradeModifier(UpgradeType.SPEED);
                for (int i = 0; i < (int) speedMult; i++) {
                    pot.growthTime.tickUp(level);
                }
                float fraction = speedMult - (int) speedMult;
                if (fraction > 0 && Math.random() < fraction) {
                    pot.growthTime.tickUp(level);
                }

                crop.onGrowthTick(pot.getRecipeContext(), level);

                int requiredTicks = pot.getRequiredGrowthTicks();
                if (requiredTicks > 0 && pot.growthTime.getTicks() >= requiredTicks) {
                    pot.updateComparatorLevel(15);
                    pot.growCooldown.setTicks(5.0F);

                    if (pot.isHopper() && crop.canHarvest(pot.getRecipeContext(), level)) {
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            int rolls = net.darkhax.botanypots.common.impl.Helpers.getLootRolls(
                                    pot.getRecipeContext(), level, crop, soil);
                            float outputMult = pot.tier.itemAmountMultiplier() * pot.getUpgradeModifier(UpgradeType.OUTPUT);

                            for (int roll = 0; roll < rolls; roll++) {
                                crop.onHarvest(pot.getRecipeContext(), level, dropStack -> {
                                    // Multiply the stack and use Math.max to prevent rounding down to 0
                                    dropStack.setCount(Math.max(1, (int)(dropStack.getCount() * outputMult)));

                                    net.darkhax.bookshelf.common.api.service.Services.GAMEPLAY
                                            .addItem(dropStack, pot.getItems(), STORAGE_SLOTS);
                                });
                            }
                        }
                        pot.growthTime.reset();
                    }
                } else if (requiredTicks > 0) {
                    pot.updateComparatorLevel(
                            net.minecraft.util.Mth.ceil(14.0F * (pot.growthTime.getTicks() / (float) requiredTicks)));
                }
            }
        }

        if (pot.isHopper()) {
            pot.exportCooldown.tickDown(level);
            if (pot.exportCooldown.getTicks() <= 0.0F) {
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    if (!serverLevel.getBlockState((BlockPos) pot.below.get()).isAir()) {
                        for (int slot : STORAGE_SLOTS) {
                            net.minecraft.world.item.ItemStack stack = pot.getItem(slot);
                            if (!stack.isEmpty()) {
                                net.minecraft.world.item.ItemStack result =
                                        net.darkhax.bookshelf.common.api.service.Services.GAMEPLAY
                                                .inventoryInsert(serverLevel, (BlockPos) pot.below.get(),
                                                        net.minecraft.core.Direction.UP, stack);
                                pot.setItem(slot, result);
                            }
                        }
                    }
                }
                pot.exportCooldown.reset();
            }
        }

        BlockPos below = pot.getBlockPos().below();

        if (pot.getLevel().getBlockEntity(below) instanceof PowerPotBlockEntity targetPot) {
            for (int potSlotId : STORAGE_SLOTS) {
                ItemStack potStack = pot.getItem(potSlotId);
                if (potStack.isEmpty()) continue;

                for (int targetSlot : STORAGE_SLOTS) {
                    ItemStack targetStack = targetPot.getItem(targetSlot);
                    if (targetStack.isEmpty()) {
                        targetPot.setItem(targetSlot, potStack.copy());
                        pot.setItem(potSlotId, ItemStack.EMPTY);
                        targetPot.setChanged();
                        break;
                    } else if (ItemStack.isSameItemSameComponents(targetStack, potStack)
                            && targetStack.getCount() < targetStack.getMaxStackSize()) {
                        int space = targetStack.getMaxStackSize() - targetStack.getCount();
                        int toMove = Math.min(space, potStack.getCount());
                        targetStack.grow(toMove);
                        potStack.shrink(toMove);
                        targetPot.setChanged();
                        if (potStack.isEmpty()) {
                            pot.setItem(potSlotId, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
            return;
        }

        pot.sync();
    }

    // ── Energy ────────────────────────────────────────────────────────────────

    public ModEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    // ── Upgrade slots ─────────────────────────────────────────────────────────

    public ItemStack getUpgradeSlot(int slot) {
        if (slot < 0 || slot >= upgradeSlots.length) return ItemStack.EMPTY;
        return upgradeSlots[slot];
    }

    public void setUpgradeSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= upgradeSlots.length) return;
        upgradeSlots[slot] = stack == null ? ItemStack.EMPTY : stack;
        setChanged();
        sync();
    }

    public float getUpgradeModifier(UpgradeType type) {
        float modifier = 1.0f;
        for (ItemStack stack : upgradeSlots) {
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                PotUpgrade potUpgrade = upgrade.getUpgrade();
                if (potUpgrade.getType() == type) {
                    modifier *= potUpgrade.getModifier();
                }
            }
        }
        return modifier;
    }

    public int getFortuneLevel() {
        int fortune = 0;
        for (ItemStack stack : upgradeSlots) {
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                PotUpgrade potUpgrade = upgrade.getUpgrade();
                if (potUpgrade.getType() == UpgradeType.FORTUNE) {
                    fortune += (int) potUpgrade.getModifier();
                }
            }
        }
        return fortune;
    }

    // ── Networking ────────────────────────────────────────────────────────────

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt, registries);
        return nbt;
    }

    public void sync() {
        if (this.level != null) {
            this.setChanged();
            for (Player player : this.level.players()) {
                if (player instanceof ServerPlayer sp) {
                    BlockPos pos = this.getBlockPos();
                    if (sp.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0
                            && !this.isRemoved()
                            && this.level.getBlockEntity(pos) == this) {
                        sp.connection.send(this.getUpdatePacket());
                    }
                }
            }
            this.level.sendBlockUpdated(
                    this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        super.saveToItem(stack, registries);
        CompoundTag blockEntityTag = new CompoundTag();

        boolean hasData = false;

        for (int i = 0; i < upgradeSlots.length; i++) {
            if (!upgradeSlots[i].isEmpty()) {
                CompoundTag slotTag = (CompoundTag) upgradeSlots[i].save(registries);
                blockEntityTag.put("upgrade_" + i, slotTag);
                blockEntityTag.putString("id", "powerpots:power_pot_be");
                hasData = true;
            }
        }

        if (this.tier.index() > 0) {
            blockEntityTag.putInt("potTier", tier.index());
            hasData = true;
        }

        if (hasData) {
            stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                    net.minecraft.world.item.component.CustomData.of(blockEntityTag));
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.put("energy", this.energyStorage.serializeNBT(registries));
        tag.putInt("potTier", this.tier.index());

        for (int i = 0; i < upgradeSlots.length; i++) {
            if (!upgradeSlots[i].isEmpty()) {
                CompoundTag slotTag = (CompoundTag) upgradeSlots[i].save(registries);
                tag.put("upgrade_" + i, slotTag);
            }
        }

        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        this.energyStorage.deserializeNBT(registries, tag.get("energy"));
        int index = tag.getInt("potTier");
        this.tier = (PotTier) Config.INSTANCE.TIERS.get(Math.max(index - 1, 0));
        this.energyStorage = new ModEnergyStorage(
                this.tier.powerStorage(), this.tier.powerStorage(), 0,
                this.energyStorage.getEnergyStored());

        for (int i = 0; i < upgradeSlots.length; i++) {
            upgradeSlots[i] = ItemStack.EMPTY;
        }

        for (int i = 0; i < upgradeSlots.length; i++) {
            if (tag.contains("upgrade_" + i)) {
                upgradeSlots[i] = ItemStack.parseOptional(registries, tag.getCompound("upgrade_" + i));
            }
        }

        super.loadAdditional(tag, registries);
    }
}
