package com.example.storagemod.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * 27スロットのインベントリデータを持ち、ItemStackのデータコンポーネント(CONTAINER)と同期・永続化するクラス
 */
public class StorageInventory extends SimpleContainer {
    public static final int INVENTORY_SIZE = 27;
    private final ItemStack containerStack;

    public StorageInventory(ItemStack containerStack) {
        super(INVENTORY_SIZE);
        this.containerStack = containerStack;
        readFromItem();
    }

    public void readFromItem() {
        ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(this.getItems());
        }
    }

    public void writeToItem() {
        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        writeToItem();
    }

    public void onClose(Player player) {
        writeToItem();
    }

    public ItemStack getContainerStack() {
        return containerStack;
    }
}
