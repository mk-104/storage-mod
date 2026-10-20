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
 * アイテム2：スマート・シュルカーバンドル（Smart Shulker Bundle）
 * 設置不要で持ち運びができる27スロットの大容量インベントリアイテム。
 */
public class SmartShulkerBundleItem extends Item {

    public SmartShulkerBundleItem(Properties properties) {
        super(properties);
    }

    public static boolean isItemAllowed(ItemStack stack) {
        if (stack.isEmpty()) return true;
        Item item = stack.getItem();

        // 大容量収納アイテムのネスト禁止フィルター
        if (item instanceof SmartShulkerBundleItem || item instanceof ArmoryCubeItem || item instanceof BundleItem) {
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
                                SmartShulkerBundleItem::isItemAllowed, selectedSlot);
                    },
                    stack.getHoverName()
            ));

            player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 1.0F);
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
        tooltip.accept(Component.translatable("tooltip.storage_mod.smart_shulker_bundle.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
