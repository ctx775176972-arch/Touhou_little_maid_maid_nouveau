package com.touhoulittlemaid.maidnouveau.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

/** Adds the Ars attributes that have meaningful non-player behavior to maids. */
public final class MaidAttributeEvents {
    private MaidAttributeEvents() {
    }

    public static void addArsAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityMaid.TYPE, PerkAttributes.MANA_REGEN_BONUS);
        event.add(EntityMaid.TYPE, PerkAttributes.SPELL_DAMAGE_BONUS);
        event.add(EntityMaid.TYPE, PerkAttributes.WARDING);
        event.add(EntityMaid.TYPE, PerkAttributes.FEATHER);
        event.add(EntityMaid.TYPE, PerkAttributes.WIXIE);
    }
}
