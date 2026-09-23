package com.touhoulittlemaid.maidnouveau.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.touhoulittlemaid.maidnouveau.registry.MaidNouveauTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Repairs equipped Ars Nouveau equipment from mana-regen gear and the Mana Regen effect. */
public final class MaidManaRegenRepairEvents {
    private static final int REPAIR_INTERVAL = 100;
    private static final double REPAIR_PER_MANA_REGEN = 1.0D;
    private static final int REPAIR_PER_EFFECT_LEVEL = 2;

    private MaidManaRegenRepairEvents() {
    }

    public static void onMaidTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof EntityMaid maid)
                || maid.level().isClientSide) {
            return;
        }

        if (maid.level().getGameTime() % REPAIR_INTERVAL != 0) {
            return;
        }

        int amount = repairAmount(maid);
        if (amount <= 0) {
            return;
        }

        maid.getHandSlots().forEach(stack -> repair(stack, amount));
        maid.getArmorSlots().forEach(stack -> repair(stack, amount));
        CuriosApi.getCuriosInventory(maid).ifPresent(handler -> {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                repair(equipped.getStackInSlot(slot), amount);
            }
        });
    }

    private static int repairAmount(EntityMaid maid) {
        // Equipment and Curios apply their Ars mana-regen modifiers directly to this attribute.
        int amount = (int) Math.floor(maid.getAttributeValue(PerkAttributes.MANA_REGEN_BONUS)
                * REPAIR_PER_MANA_REGEN);

        MobEffectInstance manaRegen = maid.getEffect(ModPotions.MANA_REGEN_EFFECT);
        if (manaRegen != null) {
            amount += REPAIR_PER_EFFECT_LEVEL * (manaRegen.getAmplifier() + 1);
        }
        return amount;
    }

    private static void repair(ItemStack stack, int amount) {
        if (stack.isEmpty() || !stack.isDamaged() || !isMagicEquipment(stack)) {
            return;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
    }

    private static boolean isMagicEquipment(ItemStack stack) {
        if (stack.is(MaidNouveauTags.Items.MANA_REGEN_REPAIRABLE)) {
            return true;
        }
        if (stack.has(DataComponentRegistry.ARMOR_PERKS)) {
            return true;
        }
        if (SpellCasterRegistry.from(stack) != null) {
            return true;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("ars_nouveau");
    }
}
