package com.example.storagemod.screen;

import com.example.storagemod.inventory.StorageInventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * 27スロットの収納アイテム用共通ScreenHandler (AbstractContainerMenu)
 * スロットへの挿入可否（itemFilter）やホットバーのロックを管理
 */
public class StorageScreenHandler extends AbstractContainerMenu {
    public static final int ROWS = 3;
    public static final int COLUMNS = 9;
    public static final int CONTAINER_SIZE = ROWS * COLUMNS; // 27スロット

    private final Container inventory;
    private final Predicate<ItemStack> itemFilter;
    private final int lockedSlotIndex;

    public StorageScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(CONTAINER_SIZE), stack -> true, -1);
    }

    public StorageScreenHandler(int syncId, Inventory playerInventory, Container inventory,
                                Predicate<ItemStack> itemFilter, int lockedSlotIndex) {
        super(ModScreenHandlers.STORAGE_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, CONTAINER_SIZE);
        this.inventory = inventory;
        this.itemFilter = itemFilter;
        this.lockedSlotIndex = lockedSlotIndex;
        inventory.startOpen(playerInventory.player);

        // 1. 収納コンテナのスロット (0 ~ 26)
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                int index = col + row * COLUMNS;
                this.addSlot(new Slot(inventory, index, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return itemFilter.test(stack);
                    }
                });
            }
        }

        // 2. プレイヤーのメインインベントリ (27 ~ 53)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 3. プレイヤーのホットバー (54 ~ 62)
        for (int col = 0; col < 9; ++col) {
            int slotIdx = col;
            this.addSlot(new Slot(playerInventory, slotIdx, 8 + col * 18, 142) {
                @Override
                public boolean mayPickup(Player player) {
                    return slotIdx != lockedSlotIndex;
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
        if (this.inventory instanceof StorageInventory storageInv) {
            storageInv.onClose(player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < CONTAINER_SIZE) {
                // コンテナ内からプレイヤーインベントリへ移動
                if (!this.moveItemStackTo(originalStack, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // プレイヤーインベントリからコンテナ内へ移動（フィルターに合格する場合のみ）
                if (!itemFilter.test(originalStack) || !this.moveItemStackTo(originalStack, 0, CONTAINER_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }
}
