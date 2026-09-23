package com.touhoulittlemaid.maidnouveau.util;

import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractCaster;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Helpers for the zero-damage carrier arrows used by scribed Ars spell weapons. */
public final class SpellWeaponAmmo {
    private static final String SYNTHETIC_ARROW_TAG = "maid_nouveau_synthetic_arrow";

    private SpellWeaponAmmo() {
    }

    public static boolean hasValidSpell(ItemStack weapon) {
        AbstractCaster<?> caster = SpellCasterRegistry.from(weapon);
        return caster != null && caster.getSpell().isValid();
    }

    public static ItemStack createSyntheticArrow() {
        ItemStack stack = Items.ARROW.getDefaultInstance();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(SYNTHETIC_ARROW_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static boolean isSyntheticArrow(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getBoolean(SYNTHETIC_ARROW_TAG);
    }
}
