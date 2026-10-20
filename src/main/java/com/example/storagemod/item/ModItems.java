package com.example.storagemod.item;

import com.example.storagemod.StorageMod;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

/**
 * Mod内全アイテムの登録・保持クラス
 */
public class ModItems {

    // 1. ディープ・モノポッド
    public static final Item DEEP_MONOPOD = register("deep_monopod",
            key -> new DeepMonopodItem(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.RARE)));

    // 2. スマート・シュルカーバンドル
    public static final Item SMART_SHULKER_BUNDLE = register("smart_shulker_bundle",
            key -> new SmartShulkerBundleItem(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.UNCOMMON)));

    // 3. アーマリー・キューブ
    public static final Item ARMORY_CUBE = register("armory_cube",
            key -> new ArmoryCubeItem(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.EPIC)));

    // Mod専用クリエイティブタブ
    public static final ResourceKey<CreativeModeTab> STORAGE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(StorageMod.MOD_ID, "storage_tab")
    );

    public static final CreativeModeTab STORAGE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            STORAGE_TAB_KEY,
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.storage_mod"))
                    .icon(() -> new ItemStack(SMART_SHULKER_BUNDLE))
                    .displayItems((params, output) -> {
                        output.accept(DEEP_MONOPOD);
                        output.accept(SMART_SHULKER_BUNDLE);
                        output.accept(ARMORY_CUBE);
                    })
                    .build()
    );

    private static Item register(String name, Function<ResourceKey<Item>, Item> itemFactory) {
        Identifier id = Identifier.fromNamespaceAndPath(StorageMod.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                itemFactory.apply(key)
        );
    }

    public static void registerModItems() {
        StorageMod.LOGGER.info("Registering items for " + StorageMod.MOD_ID);

        // バニラの道具タブにも追加
        ResourceKey<CreativeModeTab> toolsKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("tools_and_utilities")
        );
        CreativeModeTabEvents.modifyOutputEvent(toolsKey).register(output -> {
            output.accept(DEEP_MONOPOD);
            output.accept(SMART_SHULKER_BUNDLE);
            output.accept(ARMORY_CUBE);
        });
    }
}
