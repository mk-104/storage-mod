package com.example.storagemod.mixin;

import com.example.storagemod.event.ItemPickupCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * プレイヤーがアイテムに接触した際の処理にフックし、ItemPickupCallbackを発火するMixin
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void onPlayerTouch(Player player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        // サーバーサイドかつアイテムが拾える待機時間経過済みであること
        if (!itemEntity.level().isClientSide() && !itemEntity.hasPickUpDelay()) {
            InteractionResult result = ItemPickupCallback.EVENT.invoker().interact(player, itemEntity);
            if (result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME) {
                // 自動回収によりアイテムが処理されたため、通常のバニラ回収処理をキャンセル
                ci.cancel();
            }
        }
    }
}
