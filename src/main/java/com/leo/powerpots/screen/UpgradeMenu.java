package com.leo.powerpots.screen;

import com.leo.powerpots.block.entity.PowerPotBE;
import com.leo.powerpots.init.ModMenuTypes;
import com.leo.powerpots.upgrade.UpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UpgradeMenu extends AbstractContainerMenu {

    public final PowerPotBE blockEntity;
    private final ItemStackHandler upgradeHandler;

    public UpgradeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (PowerPotBE) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public UpgradeMenu(int id, Inventory inv, PowerPotBE be) {
        super(ModMenuTypes.UPGRADE_MENU.get(), id);
        this.blockEntity = be;

        // wrap upgrade slots in an ItemStackHandler for slot rendering
        this.upgradeHandler = new ItemStackHandler(3) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof UpgradeItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                // sync changes back to the block entity
                be.setUpgradeSlot(slot, getStackInSlot(slot));
                be.setChanged();
            }
        };

        // populate handler from block entity current state
        for (int i = 0; i < 3; i++) {
            upgradeHandler.setStackInSlot(i, be.getUpgradeSlot(i));
        }

        // add the 3 upgrade slots
        addSlot(new SlotItemHandler(upgradeHandler, 0, 14, 14));
        addSlot(new SlotItemHandler(upgradeHandler, 1, 14, 36));
        addSlot(new SlotItemHandler(upgradeHandler, 2, 14, 58));

        // add player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // add hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            if (index < 3) {
                // from upgrade slots to inventory
                if (!moveItemStackTo(slotStack, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // from inventory to upgrade slots
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