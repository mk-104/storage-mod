package com.example.storagemod.item;

import com.example.storagemod.inventory.StorageInventory;
import com.example.storagemod.screen.StorageScreenHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.function.Consumer;

/**
 * アイテム3：アーマリー・キューブ（Armory Cube）
 * スタック不可能な武器やツール（最大スタック数1）専用の27スロット収納キューブ。
 */
public class ArmoryCubeItem extends Item {

    public ArmoryCubeItem(Properties properties) {
        super(properties);
    }

    public static boolean isItemAllowed(ItemStack stack) {
        if (stack.isEmpty()) return true;

        // 仕様2：最大スタック数が1のアイテムのみ収納可能
        if (stack.getMaxStackSize() != 1) {
            return false;
        }

        Item item = stack.getItem();
        if (item instanceof ArmoryCubeItem || item instanceof SmartShulkerBundleItem || item instanceof BundleItem) {
            return false;
        }
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return false;
        }

        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            int selectedSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;

            player.openMenu(new SimpleMenuProvider(
                    (syncId, playerInventory, playerEntity) -> {
                        StorageInventory inv = new StorageInventory(stack);
                        return new StorageScreenHandler(syncId, playerInventory, inv,
                                ArmoryCubeItem::isItemAllowed, selectedSlot);
                    },
                    stack.getHoverName()
            ));

            player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        int occupiedCount = 0;
        if (contents != null) {
            occupiedCount = (int) contents.nonEmptyItemCopyStream().count();
        }

        tooltip.accept(Component.translatable("tooltip.storage_mod.slots_used", occupiedCount, StorageInventory.INVENTORY_SIZE)
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.storage_mod.armory_cube.desc")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
