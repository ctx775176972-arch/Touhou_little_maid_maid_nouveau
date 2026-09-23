package com.touhoulittlemaid.maidnouveau.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskCrossBowAttack;
import com.hollingsworth.arsnouveau.common.items.SpellCrossbow;
import com.touhoulittlemaid.maidnouveau.util.SpellWeaponAmmo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets a scribed Spell Crossbow enter its attack task without physical ammunition. */
@Mixin(value = TaskCrossBowAttack.class, remap = false)
public abstract class MaidCrossbowAttackMixin {
    @Inject(method = "hasAmmunition", at = @At("RETURN"), cancellable = true)
    private void maidNouveau$allowScribedSpellCrossbowWithoutArrows(EntityMaid maid,
                                                                     CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()
                && maid.getMainHandItem().getItem() instanceof SpellCrossbow
                && SpellWeaponAmmo.hasValidSpell(maid.getMainHandItem())) {
            cir.setReturnValue(true);
        }
    }
}
