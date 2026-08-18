package com.leo.powerpots.screen;

import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import com.leo.powerpots.init.ModMenuTypes;
import com.leo.powerpots.upgrade.UpgradeItem;
import com.leo.powerpots.upgrade.UpgradeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class UpgradeMenu extends AbstractContainerMenu {

    public final PowerPotBlockEntity blockEntity;
    private final ItemStackHandler upgradeHandler;

    public UpgradeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (PowerPotBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public UpgradeMenu(int id, Inventory inv, PowerPotBlockEntity be) {
        super(ModMenuTypes.UPGRADE_MENU.get(), id);
        this.blockEntity = be;

        this.upgradeHandler = new ItemStackHandler(3) {
            @Override
            public boolean isItemValid(int slot, @javax.annotation.Nonnull ItemStack stack) {
                return stack.getItem() instanceof UpgradeItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (be != null) {
                    be.setUpgradeSlot(slot, getStackInSlot(slot));
                    be.setChanged();
                }
            }
        };

        // load current upgrade state into handler
        if (be != null) {
            for (int i = 0; i < 3; i++) {
                upgradeHandler.setStackInSlot(i, be.getUpgradeSlot(i));
            }
        }

        // upgrade slots — left side of GUI
        addSlot(new SlotItemHandler(upgradeHandler, 0, 14, 14));
        addSlot(new SlotItemHandler(upgradeHandler, 1, 14, 36));
        addSlot(new SlotItemHandler(upgradeHandler, 2, 14, 58));

        // player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    public float getSpeedModifier() {
        return getModifierForType(UpgradeType.SPEED);
    }

    public float getOutputModifier() {
        return getModifierForType(UpgradeType.OUTPUT);
    }

    public float getEnergyModifier() {
        return getModifierForType(UpgradeType.ENERGY);
    }

    public int getFortuneLevel() {
        int fortune = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getUpgrade().getType() == UpgradeType.FORTUNE) {
                    fortune += (int) upgrade.getUpgrade().getModifier();
                }
            }
        }
        return fortune;
    }

    private float getModifierForType(UpgradeType type) {
        float modifier = 1.0f;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getUpgrade().getType() == type) {
                    modifier *= upgrade.getUpgrade().getModifier();
                }
            }
        }
        return modifier;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            if (index < 3) {
                if (!moveItemStackTo(slotStack, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(slotStack, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return returnStack;
    }
}