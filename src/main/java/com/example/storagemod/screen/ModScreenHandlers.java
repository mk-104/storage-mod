package com.example.storagemod.screen;

import com.example.storagemod.StorageMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * MenuType (ScreenHandlerType) の登録クラス
 */
public class ModScreenHandlers {
    public static final MenuType<StorageScreenHandler> STORAGE_SCREEN_HANDLER =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(StorageMod.MOD_ID, "storage"),
                    new MenuType<>(StorageScreenHandler::new, FeatureFlags.VANILLA_SET)
            );

    public static void registerScreenHandlers() {
        // クラス初期化
    }
}
