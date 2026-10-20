package com.example.storagemod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * アイテム1：ディープ・モノポッド（Deep Monopod）
 * 1種類のアイテムのみを最大9999個まで収納できるアイテム。
 */
public class DeepMonopodItem extends Item {
    public static final int MAX_STORAGE = 9999;
    public static final String NBT_STORED_ITEM = "StoredItem";
    public static final String NBT_STORED_COUNT = "StoredCount";

    public DeepMonopodItem(Properties properties) {
        super(properties);
    }

    public static Item getStoredItem(ItemStack monopodStack) {
        CustomData customData = monopodStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains(NBT_STORED_ITEM)) {
                String itemId = nbt.getString(NBT_STORED_ITEM).orElse("");
                if (!itemId.isEmpty()) {
                    return BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                }
            }
        }
        return Items.AIR;
    }

    public static int getStoredCount(ItemStack monopodStack) {
        CustomData customData = monopodStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains(NBT_STORED_COUNT)) {
                return nbt.getInt(NBT_STORED_COUNT).orElse(0);
            }
        }
        return 0;
    }

    public static void setStored(ItemStack monopodStack, Item item, int count) {
        CustomData customData = monopodStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();
        if (item == Items.AIR || count <= 0) {
            nbt.remove(NBT_STORED_ITEM);
            nbt.remove(NBT_STORED_COUNT);
        } else {
            nbt.putString(NBT_STORED_ITEM, BuiltInRegistries.ITEM.getKey(item).toString());
            nbt.putInt(NBT_STORED_COUNT, Math.min(count, MAX_STORAGE));
        }
        CustomData.set(DataComponents.CUSTOM_DATA, monopodStack, nbt);
    }

    public static void setStoredCount(ItemStack monopodStack, int count) {
        setStored(monopodStack, getStoredItem(monopodStack), count);
    }

    /**
     * 地上に落ちているアイテムをモノポッドに自動吸収する処理
     */
    public static boolean tryAbsorbItem(ItemStack monopodStack, ItemEntity itemEntity, Player player) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return false;
        }

        Item storedItem = getStoredItem(monopodStack);
        int currentCount = getStoredCount(monopodStack);

        // 未登録の場合は、拾ったアイテムを最初の1種類として登録して吸収
        if (storedItem == Items.AIR) {
            storedItem = groundStack.getItem();
        }

        // 登録されているアイテムと地上アイテムが一致するか確認
        if (storedItem == groundStack.getItem() && currentCount < MAX_STORAGE) {
            int space = MAX_STORAGE - currentCount;
            int absorbAmount = Math.min(space, groundStack.getCount());

            // モノポッドのNBTを更新
            setStored(monopodStack, storedItem, currentCount + absorbAmount);

            // 地上のアイテム数を減らす
            groundStack.shrink(absorbAmount);

            // 拾得音とバニラの拾得エフェクトパケット
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                    0.2F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
            player.take(itemEntity, absorbAmount);

            // 地上のアイテムが0になったら消滅
            if (groundStack.isEmpty()) {
                itemEntity.discard();
            }
            return true;
        }

        return false;
    }

    /**
     * ブロックを右クリックした際の処理：
     * 収納アイテムが設置可能なBlockItemの場合、1個ずつブロックとして設置する。
     */
    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack monopodStack = context.getItemInHand();

        // スニーク中は設置ではなく取り出し操作を優先するためPASS
        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Item storedItem = getStoredItem(monopodStack);
        int currentCount = getStoredCount(monopodStack);

        // 収納アイテムがBlockItemで個数が残っている場合
        if (storedItem instanceof net.minecraft.world.item.BlockItem blockItem && currentCount > 0) {
            // 設置用の仮想スタック（1個分）を作成
            ItemStack virtualStack = new ItemStack(blockItem, 1);
            net.minecraft.world.phys.BlockHitResult hitResult = new net.minecraft.world.phys.BlockHitResult(
                    context.getClickLocation(),
                    context.getClickedFace(),
                    context.getClickedPos(),
                    context.isInside()
            );
            net.minecraft.world.item.context.BlockPlaceContext placeContext = new net.minecraft.world.item.context.BlockPlaceContext(
                    player,
                    context.getHand(),
                    virtualStack,
                    hitResult
            );

            // ブロック設置を実行
            InteractionResult result = blockItem.place(placeContext);
            if (result.consumesAction()) {
                // クリエイティブモードでなければモノポッド内のカウントを1減算
                if (!level.isClientSide() && (player == null || !player.getAbilities().instabuild)) {
                    setStoredCount(monopodStack, currentCount - 1);
                }
                return result;
            }
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack monopodStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // 仕様3：スニーク＋右クリックで収納アイテムを1スタック取り出す
        if (player.isShiftKeyDown()) {
            Item storedItem = getStoredItem(monopodStack);
            int currentCount = getStoredCount(monopodStack);

            if (storedItem != Items.AIR && currentCount > 0) {
                int extractAmount = Math.min(storedItem.getDefaultMaxStackSize(), currentCount);
                ItemStack extractedStack = new ItemStack(storedItem, extractAmount);

                if (!player.getInventory().add(extractedStack)) {
                    ItemEntity dropEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), extractedStack);
                    level.addFreshEntity(dropEntity);
                }

                int remainingInStack = extractedStack.getCount();
                int actuallyExtracted = extractAmount - remainingInStack;
                int newCount = currentCount - actuallyExtracted;
                setStoredCount(monopodStack, newCount);

                player.sendSystemMessage(Component.translatable("message.storage_mod.deep_monopod.extracted",
                        actuallyExtracted, Component.translatable(storedItem.getDescriptionId())));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);

                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.translatable("message.storage_mod.deep_monopod.empty"));
            }
        } else {
            // スニークなし右クリック：オフハンドのアイテムを手動で預け入れ
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (!otherStack.isEmpty()) {
                Item storedItem = getStoredItem(monopodStack);
                int currentCount = getStoredCount(monopodStack);

                if (storedItem == Items.AIR || storedItem == otherStack.getItem()) {
                    int space = MAX_STORAGE - currentCount;
                    if (space > 0) {
                        int depositAmount = Math.min(space, otherStack.getCount());
                        setStored(monopodStack, otherStack.getItem(), currentCount + depositAmount);
                        otherStack.shrink(depositAmount);

                        player.sendSystemMessage(Component.translatable("message.storage_mod.deep_monopod.deposited",
                                depositAmount, Component.translatable(otherStack.getItem().getDescriptionId())));
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 0.9F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Item storedItem = getStoredItem(stack);
        int currentCount = getStoredCount(stack);

        if (storedItem != Items.AIR && currentCount > 0) {
            tooltip.accept(Component.translatable(storedItem.getDescriptionId())
                    .append(Component.literal(": " + currentCount + " / " + MAX_STORAGE))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.accept(Component.translatable("tooltip.storage_mod.deep_monopod.empty")
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.accept(Component.translatable("tooltip.storage_mod.deep_monopod.help_pickup")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("tooltip.storage_mod.deep_monopod.help_place")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("tooltip.storage_mod.deep_monopod.help_extract")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredCount(stack) > 0;
    }
}
