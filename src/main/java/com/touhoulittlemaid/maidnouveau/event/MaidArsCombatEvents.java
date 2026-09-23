package com.touhoulittlemaid.maidnouveau.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/** Player-only Ars combat hooks mirrored for Touhou Little Maid entities. */
public final class MaidArsCombatEvents {
    private static final int SHIELD_BUFF_DURATION = 200;
    private static final int SHIELD_BUFF_AMPLIFIER = 1;
    private static final float SPELL_DAMAGE_PER_LEVEL = 2.0F;
    private static final float[] FAVORABILITY_SPELL_DAMAGE = {0.0F, 3.0F, 5.0F, 7.0F};

    private MaidArsCombatEvents() {
    }

    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid)
                || maid.level().isClientSide
                || !maid.isBlocking()
                || !maid.getUseItem().is(ItemsRegistry.ENCHANTERS_SHIELD.get())) {
            return;
        }

        maid.addEffect(new MobEffectInstance(
                ModPotions.MANA_REGEN_EFFECT,
                SHIELD_BUFF_DURATION,
                SHIELD_BUFF_AMPLIFIER
        ));
        maid.addEffect(new MobEffectInstance(
                ModPotions.SPELL_DAMAGE_EFFECT,
                SHIELD_BUFF_DURATION,
                SHIELD_BUFF_AMPLIFIER
        ));
    }

    public static void onSpellDamage(SpellDamageEvent.Pre event) {
        if (!(event.caster instanceof EntityMaid maid)) {
            return;
        }

        int favorabilityLevel = maid.getFavorabilityManager().getLevel();
        event.damage += FAVORABILITY_SPELL_DAMAGE[Math.clamp(favorabilityLevel, 0, FAVORABILITY_SPELL_DAMAGE.length - 1)];

        MobEffectInstance effect = maid.getEffect(ModPotions.SPELL_DAMAGE_EFFECT);
        if (effect != null) {
            event.damage += SPELL_DAMAGE_PER_LEVEL * (effect.getAmplifier() + 1);
        }
    }
}
