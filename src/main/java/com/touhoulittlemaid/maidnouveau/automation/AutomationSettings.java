package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public final class AutomationSettings {
    public static final int MIN_SECONDS = 5;
    public static final int MAX_SECONDS = 3600;
    public static final int DEFAULT_SECONDS = 5;
    private static final String ROOT = "MaidNouveauAutomation";

    private AutomationSettings() {
    }

    public static boolean enabled(EntityMaid maid, WorkstationType type) {
        return typeTag(maid, type, false).getBoolean("Enabled");
    }

    public static int seconds(EntityMaid maid, WorkstationType type) {
        CompoundTag tag = typeTag(maid, type, false);
        return tag.contains("Seconds") ? Mth.clamp(tag.getInt("Seconds"), MIN_SECONDS, MAX_SECONDS) : DEFAULT_SECONDS;
    }

    public static void set(EntityMaid maid, WorkstationType type, boolean enabled, int seconds) {
        CompoundTag tag = typeTag(maid, type, true);
        tag.putBoolean("Enabled", enabled);
        tag.putInt("Seconds", Mth.clamp(seconds, MIN_SECONDS, MAX_SECONDS));
    }

    public static boolean ready(EntityMaid maid, WorkstationType type, long gameTime) {
        return !enabled(maid, type) || gameTime >= typeTag(maid, type, false).getLong("NextWorkTime");
    }

    public static void complete(EntityMaid maid, WorkstationType type, long gameTime) {
        if (enabled(maid, type)) {
            typeTag(maid, type, true).putLong("NextWorkTime", gameTime + seconds(maid, type) * 20L);
        }
    }

    private static CompoundTag typeTag(EntityMaid maid, WorkstationType type, boolean create) {
        CompoundTag persistent = maid.getPersistentData();
        CompoundTag root = persistent.getCompound(ROOT);
        CompoundTag typeData = root.getCompound(type.id());
        if (create) {
            root.put(type.id(), typeData);
            persistent.put(ROOT, root);
        }
        return typeData;
    }
}
