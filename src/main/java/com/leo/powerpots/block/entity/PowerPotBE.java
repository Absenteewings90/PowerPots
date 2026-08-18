package com.leo.powerpots.block.entity;

import com.google.gson.annotations.Expose;
import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.PotTier;
import com.leo.powerpots.config.Config;
import com.leo.powerpots.energy.ModEnergyStorage;
import com.leo.powerpots.init.ModBlockEntities;
import com.leo.powerpots.upgrade.UpgradeItem;
import com.leo.powerpots.upgrade.UpgradeType;
import net.darkhax.bookshelf.api.Services;
import net.darkhax.bookshelf.api.inventory.ContainerInventoryAccess;
import net.darkhax.bookshelf.api.inventory.IInventoryAccess;
import net.darkhax.botanypots.BotanyPotHelper;
import net.darkhax.botanypots.Constants;
import net.darkhax.botanypots.block.BlockEntityBotanyPot;
import net.darkhax.botanypots.block.inv.BotanyPotContainer;
import net.darkhax.botanypots.data.recipes.crop.Crop;
import net.darkhax.botanypots.data.recipes.soil.Soil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.protocol.Packet;

import java.util.List;
import java.util.Random;


public class PowerPotBE extends BlockEntityBotanyPot {
    final Random rng = new Random();
    private long rngSeed;

    public ModEnergyStorage energyHandler;
    private LazyOptional<ModEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    private PotTier tier;

    public PotTier getTier() {
        return tier;
    }

    public boolean skipDrop = false;

    private int syncTimer = 0;
    private static final int SYNC_INTERVAL = 5;

    private static final int UPGRADE_SLOTS = 3;
    private final ItemStack[] upgradeSlots = new ItemStack[UPGRADE_SLOTS];

    public PowerPotBE(BlockPos pPos, BlockState pBlockState, PotTier tier) {
        super(ModBlockEntities.POWER_POT_BE.get(), pPos, pBlockState);
        energyHandler = new ModEnergyStorage(tier.powerStorage(), tier.powerStorage(), 0, 0);
        customRefreshRandom();
        this.tier = tier;
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            upgradeSlots[i] = ItemStack.EMPTY;
        }
    }

    public PowerPotBE(BlockPos pPos, BlockState pState) {
        this(pPos, pState, PotTier.ZERO);
    }

    public void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.setChanged();
            for (net.minecraft.world.entity.player.Player player : this.level.players()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    BlockPos pos = this.getBlockPos();
                    if (sp.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0
                            && !this.isRemoved()
                            && this.level.getBlockEntity(pos) == this) {
                        sp.connection.send(this.getUpdatePacket());
                    }
                }
            }
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static void tickPot(Level level, BlockPos pos, BlockState state, PowerPotBE pot) {
        pot.sync();
        if (pot.isRemoved() || pot.getLevel() == null) {
            return;
        }

        pot.getInventory().update();

        final Soil soil = pot.getSoil();
        final Crop crop = pot.getCrop();

        if (soil != null) {
            soil.onTick(level, pos, pot);
        }

        if (crop != null) {
            crop.onTick(level, pos, pot);
        }

        if (pot.isHopper()) {
            if (pot.exportDelay > 0) {
                pot.exportDelay--;
            }

            if (pot.harvestDelay > 0) {
                pot.harvestDelay--;
            }

            if (crop != null && pot.harvestDelay < 1 && pot.isCropHarvestable()) {
                if (pot.attemptAutoHarvest()) {
                    pot.resetGrowth();
                }

                pot.harvestDelay = 50;
            }

            if (pot.exportDelay < 1) {
                pot.attemptExport();
                pot.exportDelay = 10;
            }
        }

        // Growth Logic
        if (soil != null && crop != null && pot.areGrowthConditionsMet()) {
            if (!pot.doneGrowing) {

                int speedBonus = (int)(pot.tier.speedModifier() * pot.getUpgradeModifier(UpgradeType.SPEED));
                pot.growthTime += speedBonus;

                if (pot.growthTime % 100 == 0) {
                    PowerPots.LOGGER.info("Growth progress: {}/{} at {}",
                            pot.growthTime,
                            pot.getInventory().getRequiredGrowthTime(),
                            pos);
                }

                int energyCost = Math.min(
                        (int)(pot.tier.powerEachTick() * pot.getUpgradeModifier(UpgradeType.ENERGY)),
                        pot.energyHandler.getEnergyStored() // never drain more than available
                );

                if (pot.energyHandler.getEnergyStored() < pot.tier.powerEachTick()) {
                    PowerPots.LOGGER.info("NOT GROWING - out of energy at {}: {}/{}",
                            pos,
                            pot.energyHandler.getEnergyStored(),
                            pot.tier.powerEachTick());
                }

                pot.energyHandler.removeEnergy(energyCost);

                soil.onGrowthTick(level, pos, pot, crop);
                crop.onGrowthTick(level, pos, pot, soil);

                pot.prevComparatorLevel = pot.comparatorLevel;
                pot.comparatorLevel = Mth.floor(15f * ((float) pot.growthTime / pot.getInventory().getRequiredGrowthTime()));

                final boolean finishedGrowing = pot.growthTime >= pot.getInventory().getRequiredGrowthTime();

                if (pot.doneGrowing != finishedGrowing) {
                    pot.doneGrowing = finishedGrowing;
                }
            }
        }

        else if (pot.growthTime != -1 || pot.doneGrowing || pot.comparatorLevel != 0) {
            pot.resetGrowth();
            pot.sync();
        }

        if (pot.comparatorLevel != pot.prevComparatorLevel) {
            pot.prevComparatorLevel = pot.comparatorLevel;
            pot.level.updateNeighbourForOutputSignal(pot.worldPosition, pot.getBlockState().getBlock());
        }
    }

    public void customRefreshRandom() {
        this.rngSeed = Constants.RANDOM.nextLong();
        this.rng.setSeed(rngSeed);
    }

    public ItemStack[] getUpgradeSlots() {
        return upgradeSlots;
    }

    public ItemStack getUpgradeSlot(int index) {
        return upgradeSlots[index];
    }

    public void setUpgradeSlot(int index, ItemStack stack) {
        upgradeSlots[index] = stack;
    }

    public float getUpgradeModifier(UpgradeType type) {
        float total = 1.0f;
        for (ItemStack stack : upgradeSlots) {
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getUpgrade().getType() == type) {
                    total *= upgrade.getUpgrade().getModifier();
                }
            }
        }
        return total;
    }

    public int getFortuneLevel() {
        int fortune = 0;
        for (ItemStack stack : upgradeSlots) {
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getUpgrade().getType() == UpgradeType.FORTUNE) {
                    fortune += (int) upgrade.getUpgrade().getModifier();
                }
            }
        }
        return fortune;
    }

    @Override
    public boolean attemptAutoHarvest() {
        if (this.getLevel() == null || this.getLevel().isClientSide || this.getCrop() == null) {
            return false;
        }

        final ContainerInventoryAccess<BotanyPotContainer> inventory =
                new ContainerInventoryAccess<>(this.getInventory());

        this.rng.setSeed(this.rngSeed);

        // Wrap the generated drops in a new ArrayList so we can safely modify it
        final List<ItemStack> drops = new java.util.ArrayList<>(BotanyPotHelper.generateDrop(
                rng, this.level, this.getBlockPos(), this, this.getCrop()));

        // Refresh the random seed so the next cycle isn't locked
        this.customRefreshRandom();

        // --- NEW CODE: GUARANTEE A SEED DROP ---
        ItemStack seedStack = ItemStack.EMPTY;

        // Grab the physical seed item directly from the Pot's Seed slot (Slot 1)
        ItemStack potSeed = this.getInventory().getItem(1);
        if (!potSeed.isEmpty()) {
            seedStack = potSeed.copy();
            seedStack.setCount(1); // Ensure we only add 1 to the base drop pool
        }

        // Check if BotanyPots RNG already gave us a seed naturally
        boolean hasSeed = false;
        for (ItemStack drop : drops) {
            if (ItemStack.isSameItem(drop, seedStack)) {
                hasSeed = true;
                break;
            }
        }

        // If the RNG didn't give us a seed, force it into the drops list!
        if (!hasSeed && !seedStack.isEmpty()) {
            drops.add(seedStack);
        }
        // ---------------------------------------

        if (drops.isEmpty()) return true;

        boolean didCollect = false;

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;

            // ← work on a copy so the for-each variable is never affected
            ItemStack toInsert = drop.copy();

            // apply multipliers — Math.max(1, ...) prevents rounding to 0
            float outputMult = tier.itemAmountMultiplier()
                    * getUpgradeModifier(UpgradeType.OUTPUT);
            toInsert.setCount(Math.max(1, (int)(toInsert.getCount() * outputMult)));

            // fortune bonus
            int fortuneBonus = getFortuneLevel();
            if (fortuneBonus > 0) {
                toInsert.grow(rng.nextInt(fortuneBonus + 1));
            }

            final int originalCount = toInsert.getCount();

            for (int slot : BotanyPotContainer.STORAGE_SLOT) {
                if (toInsert.isEmpty()) break;
                toInsert = inventory.insert(slot, toInsert, Direction.UP, true, true);
            }

            if (toInsert.getCount() != originalCount) {
                didCollect = true;
            }
        }

        return didCollect;
    }

    private void attemptExport() {
        if (this.getLevel() == null || this.getLevel().isClientSide) {
            return;
        }

        BlockPos below = this.getBlockPos().below();

        if (this.getLevel().getBlockEntity(below) instanceof PowerPotBE targetPot) {
            for (int potSlotId : BotanyPotContainer.STORAGE_SLOT) {
                ItemStack potStack = this.getInventory().getItem(potSlotId);
                if (potStack.isEmpty()) continue;

                for (int targetSlot : BotanyPotContainer.STORAGE_SLOT) {
                    ItemStack targetStack = targetPot.getInventory().getItem(targetSlot);

                    if (targetStack.isEmpty()) {
                        targetPot.getInventory().setItem(targetSlot, potStack.copy());
                        this.getInventory().setItem(potSlotId, ItemStack.EMPTY);
                        targetPot.setChanged();
                        break;
                    } else if (ItemStack.isSameItemSameTags(targetStack, potStack)
                            && targetStack.getCount() < targetStack.getMaxStackSize()) {
                        int space = targetStack.getMaxStackSize() - targetStack.getCount();
                        int toMove = Math.min(space, potStack.getCount());
                        targetStack.grow(toMove);
                        potStack.shrink(toMove);
                        targetPot.setChanged();
                        if (potStack.isEmpty()) {
                            this.getInventory().setItem(potSlotId, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
            return;
        }

        final IInventoryAccess exportTo = Services.INVENTORY_HELPER.getInventory(this.getLevel(), this.getBlockPos().below(), Direction.UP);

        if (exportTo == null) {
            return;
        }

        for (int potSlotId : BotanyPotContainer.STORAGE_SLOT) {
            final ItemStack potStack = this.getInventory().getItem(potSlotId);

            if (!potStack.isEmpty()) {
                for (int exportSlotId : exportTo.getAvailableSlots()) {
                    if (exportTo.insert(exportSlotId, potStack, Direction.UP, false).getCount() != potStack.getCount()) {
                        this.getInventory().setItem(potSlotId, exportTo.insert(exportSlotId, potStack, Direction.UP, true));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergyHandler = LazyOptional.of(() -> energyHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public boolean areGrowthConditionsMet() {
        return super.areGrowthConditionsMet() && energyHandler.getEnergyStored() >= tier.powerEachTick();
    }

    @Override
    public void saveToItem(ItemStack stack) {
        super.saveToItem(stack);
        CompoundTag blockEntityTag = new CompoundTag();

        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            if (!upgradeSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                upgradeSlots[i].save(slotTag);
                blockEntityTag.put("upgrade_" + i, slotTag);
                PowerPots.LOGGER.info("Saving upgrade slot {} to item: {}", i, upgradeSlots[i]);
            }
        }
        blockEntityTag.putInt("potTier", tier.index());
        stack.addTagElement("BlockEntityTag", blockEntityTag);
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        pTag.put("energy", energyHandler.serializeNBT());
        pTag.putInt("potTier", tier.index());

        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            if (!upgradeSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                upgradeSlots[i].save(slotTag);
                pTag.put("upgrade_" + i, slotTag);
            }
        }

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        if (pTag.contains("energy")) {
            energyHandler.deserializeNBT(pTag.get("energy"));
        }

        int index = pTag.getInt("potTier");
        tier = Config.INSTANCE.TIERS.get(Math.max(index - 1, 0));
        energyHandler = new ModEnergyStorage(
                tier.powerStorage(), tier.powerStorage(), 0,
                energyHandler.getEnergyStored()
        );

        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            if (pTag.contains("upgrade_" + i)) {
                upgradeSlots[i] = ItemStack.of(pTag.getCompound("upgrade_" + i));
            } else {
                upgradeSlots[i] = ItemStack.EMPTY;
            }
        }

        super.load(pTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
