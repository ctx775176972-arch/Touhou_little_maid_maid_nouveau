package com.touhoulittlemaid.maidnouveau.automation;

import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.block.tile.PotionJarTile;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum WorkstationType {
    APPARATUS("apparatus", EnchantingApparatusTile.class),
    POTION_JAR("potion_jar", PotionJarTile.class),
    RITUAL_BRAZIER("ritual_brazier", RitualBrazierTile.class);

    private final String id;
    private final Class<? extends BlockEntity> blockEntityClass;

    WorkstationType(String id, Class<? extends BlockEntity> blockEntityClass) {
        this.id = id;
        this.blockEntityClass = blockEntityClass;
    }

    public String id() {
        return id;
    }

    public boolean matches(BlockEntity blockEntity) {
        return blockEntityClass.isInstance(blockEntity);
    }

    public static WorkstationType fromBlockEntity(BlockEntity blockEntity) {
        for (WorkstationType type : values()) {
            if (type.matches(blockEntity)) {
                return type;
            }
        }
        return null;
    }

    public static WorkstationType fromId(String id) {
        for (WorkstationType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
