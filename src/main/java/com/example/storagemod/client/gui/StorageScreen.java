package com.example.storagemod.client.gui;

import com.example.storagemod.screen.StorageScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 27スロット収納アイテム用カスタムScreen
 * バニラのチェストGUIテクスチャ（generic_54.png）を利用して3行分を描画
 */
public class StorageScreen extends AbstractContainerScreen<StorageScreenHandler> {
    private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private final int containerRows = 3;

    public StorageScreen(StorageScreenHandler menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 114 + 3 * 18); // 168ピクセル
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractBackground(extractor, mouseX, mouseY, delta);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // コンテナの3行分（上部ヘッダー + スロット3行）
        extractor.blit(
                RenderPipelines.GUI_TEXTURED,
                CONTAINER_BACKGROUND,
                x, y,
                0.0f, 0.0f,
                this.imageWidth, this.containerRows * 18 + 17,
                256, 256
        );

        // プレイヤーインベントリ部分（下部スロット3行 + ホットバー）
        extractor.blit(
                RenderPipelines.GUI_TEXTURED,
                CONTAINER_BACKGROUND,
                x, y + this.containerRows * 18 + 17,
                0.0f, 126.0f,
                this.imageWidth, 96,
                256, 256
        );
    }
}
