package com.touhoulittlemaid.maidnouveau.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MaidNouveauConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue SEARCH_RADIUS;
    public static final ModConfigSpec.DoubleValue MOVE_SPEED;
    public static final ModConfigSpec.IntValue APPARATUS_WORK_INTERVAL;
    public static final ModConfigSpec.IntValue POTION_JAR_WORK_INTERVAL;
    public static final ModConfigSpec.IntValue RITUAL_WORK_INTERVAL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Maid Nouveau automation settings").push("automation");
        SEARCH_RADIUS = builder.comment("Horizontal range used when no workstation is bound.")
                .defineInRange("searchRadius", 16, 4, 64);
        MOVE_SPEED = builder.comment("Movement speed while approaching an Ars Nouveau workstation.")
                .defineInRange("moveSpeed", 0.6D, 0.1D, 2.0D);
        APPARATUS_WORK_INTERVAL = interval(builder, "apparatusWorkInterval", 20);
        POTION_JAR_WORK_INTERVAL = interval(builder, "potionJarWorkInterval", 20);
        RITUAL_WORK_INTERVAL = interval(builder, "ritualWorkInterval", 20);
        builder.pop();
        SPEC = builder.build();
    }

    private static ModConfigSpec.IntValue interval(ModConfigSpec.Builder builder, String name, int value) {
        return builder.comment("Minimum ticks between maid operations for this task.")
                .defineInRange(name, value, 1, 1200);
    }

    private MaidNouveauConfig() {
    }
}
