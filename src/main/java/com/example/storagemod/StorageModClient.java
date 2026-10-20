package com.example.storagemod;

import com.example.storagemod.client.gui.StorageScreen;
import com.example.storagemod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * クライアント側初期化クラス
 * MenuTypeとGUI Screenのバインドを行う
 */
@Environment(EnvType.CLIENT)
public class StorageModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.STORAGE_SCREEN_HANDLER, StorageScreen::new);
    }
}
