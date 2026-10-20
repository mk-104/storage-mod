package com.example.storagemod;

import com.example.storagemod.event.ItemPickupCallback;
import com.example.storagemod.item.DeepMonopodItem;
import com.example.storagemod.item.ModItems;
import com.example.storagemod.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StorageMod メイン初期化クラス
 */
public class StorageMod implements ModInitializer {
    public static final String MOD_ID = "storage_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Storage Mod...");

        // 1. アイテム登録
        ModItems.registerModItems();

        // 2. ScreenHandler登録
        ModScreenHandlers.registerScreenHandlers();

        // 3. アイテム拾得イベント（ディープ・モノポッドの自動回収）の登録
        registerItemPickupEvent();

        LOGGER.info("Storage Mod successfully initialized.");
    }

    /**
     * ItemPickupCallback イベントリスナーの登録
     * プレイヤーがアイテムに接触した際、インベントリ内のディープ・モノポッドを検知して自動吸収する
     */
    private void registerItemPickupEvent() {
        ItemPickupCallback.EVENT.register((player, itemEntity) -> {
            if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty()) {
                return InteractionResult.PASS;
            }

            Inventory inventory = player.getInventory();

            // プレイヤーの全スロットを走査
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.getItem() instanceof DeepMonopodItem) {
                    boolean absorbed = DeepMonopodItem.tryAbsorbItem(stack, itemEntity, player);
                    // 地上のアイテムが完全にモノポッド内に吸収された場合
                    if (absorbed && itemEntity.getItem().isEmpty()) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            return InteractionResult.PASS;
        });
    }
}
