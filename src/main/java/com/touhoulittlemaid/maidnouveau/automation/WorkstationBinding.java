package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record WorkstationBinding(ResourceKey<Level> dimension, BlockPos pos) {
    private static final String ROOT = "maid_nouveau_workstations";

    public static void set(EntityMaid maid, WorkstationType type, WorkstationBinding binding) {
        CompoundTag root = maid.getPersistentData().getCompound(ROOT);
        CompoundTag station = new CompoundTag();
        station.putString("dimension", binding.dimension.location().toString());
        station.putLong("pos", binding.pos.asLong());
        root.put(type.id(), station);
        maid.getPersistentData().put(ROOT, root);
    }

    public static Optional<WorkstationBinding> get(EntityMaid maid, WorkstationType type) {
        CompoundTag root = maid.getPersistentData().getCompound(ROOT);
        if (!root.contains(type.id())) {
            return Optional.empty();
        }
        CompoundTag station = root.getCompound(type.id());
        ResourceLocation dimension = ResourceLocation.tryParse(station.getString("dimension"));
        if (dimension == null) {
            return Optional.empty();
        }
        return Optional.of(new WorkstationBinding(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension),
                BlockPos.of(station.getLong("pos"))));
    }
}
