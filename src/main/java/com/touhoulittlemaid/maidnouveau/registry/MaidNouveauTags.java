package com.touhoulittlemaid.maidnouveau.registry;

import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class MaidNouveauTags {
    private MaidNouveauTags() {
    }

    public static final class Items {
        public static final TagKey<Item> MANA_REGEN_REPAIRABLE = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MaidNouveau.MOD_ID, "mana_regen_repairable")
        );

        private Items() {
        }
    }
}
