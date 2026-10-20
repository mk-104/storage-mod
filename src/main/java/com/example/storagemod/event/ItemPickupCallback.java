package com.example.storagemod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーがアイテムエンティティに接触し、拾おうとした際に発火するカスタムイベント
 */
public interface ItemPickupCallback {
    Event<ItemPickupCallback> EVENT = EventFactory.createArrayBacked(ItemPickupCallback.class,
        (listeners) -> (player, entity) -> {
            for (ItemPickupCallback listener : listeners) {
                InteractionResult result = listener.interact(player, entity);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        }
    );

    /**
     * @param player 拾おうとしているプレイヤー
     * @param entity 地上のアイテムエンティティ
     * @return SUCCESS/CONSUME: 拾得完了としてバニラ処理をスキップ / PASS: 通常の拾得処理を継続
     */
    InteractionResult interact(Player player, ItemEntity entity);
}
