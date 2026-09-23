package com.touhoulittlemaid.maidnouveau.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoulittlemaid.maidnouveau.automation.WorkstationBinding;
import com.touhoulittlemaid.maidnouveau.automation.WorkstationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WorkstationManagerItem extends Item {
    public WorkstationManagerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        WorkstationType type = WorkstationType.fromBlockEntity(context.getLevel().getBlockEntity(context.getClickedPos()));
        if (type == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type.id());
            tag.putString("dimension", context.getLevel().dimension().location().toString());
            tag.putLong("pos", context.getClickedPos().asLong());
            context.getItemInHand().set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            context.getPlayer().sendSystemMessage(Component.translatable("message.touhou_little_maid_maid_nouveau.station_selected",
                    Component.translatable("workstation.touhou_little_maid_maid_nouveau." + type.id()), context.getClickedPos().toShortString()));
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof EntityMaid maid) || !maid.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        WorkstationType type = parseType(tag.getString("type"));
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("dimension"));
        if (type == null || dimensionId == null || !tag.contains("pos")) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.touhou_little_maid_maid_nouveau.station_not_selected"));
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        if (!player.level().isClientSide) {
            ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            WorkstationBinding.set(maid, type, new WorkstationBinding(dimension, pos));
            player.sendSystemMessage(Component.translatable("message.touhou_little_maid_maid_nouveau.station_bound",
                    maid.getDisplayName(), Component.translatable("workstation.touhou_little_maid_maid_nouveau." + type.id()), pos.toShortString()));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static WorkstationType parseType(String id) {
        for (WorkstationType type : WorkstationType.values()) {
            if (type.id().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
