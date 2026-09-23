package com.touhoulittlemaid.maidnouveau.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.hollingsworth.arsnouveau.api.spell.AbstractCaster;
import com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.IWrappedCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntitySpellArrow;
import com.hollingsworth.arsnouveau.common.items.SpellArrow;
import com.hollingsworth.arsnouveau.common.items.SpellCrossbow;
import com.touhoulittlemaid.maidnouveau.util.SpellWeaponAmmo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Ars Nouveau's Spell Crossbow draw ammunition from a maid's usable inventory.
 *
 * <p>Touhou Little Maid checks the maid inventory before starting its crossbow task,
 * while {@link SpellCrossbow} loads through {@link LivingEntity#getProjectile(ItemStack)}.
 * A maid therefore sees an arrow and starts charging, but the spell crossbow receives an
 * empty stack and never stores charged projectiles. The task then repeats that empty charge.
 */
@Mixin(value = SpellCrossbow.class, remap = false)
public abstract class SpellCrossbowMixin {
    @Unique
    private static final ThreadLocal<MaidNouveauAmmoUse> MAID_NOUVEAU_AMMO_USE = new ThreadLocal<>();

    @Inject(method = "tryLoadProjectiles", at = @At("HEAD"))
    private void maidNouveau$clearAmmoContext(LivingEntity shooter, ItemStack crossbowStack,
                                               CallbackInfoReturnable<Boolean> cir) {
        MAID_NOUVEAU_AMMO_USE.remove();
    }

    @Redirect(
            method = "tryLoadProjectiles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            )
    )
    private ItemStack maidNouveau$getMaidProjectile(LivingEntity shooter, ItemStack crossbowStack) {
        if (!(shooter instanceof EntityMaid maid)
                || !(crossbowStack.getItem() instanceof SpellCrossbow spellCrossbow)) {
            return shooter.getProjectile(crossbowStack);
        }

        CombinedInvWrapper inventory = maid.getAvailableInv(true);
        int slot = ItemsUtil.findStackSlot(inventory, spellCrossbow.getAllSupportedProjectiles());
        if (slot < 0) {
            return SpellWeaponAmmo.hasValidSpell(crossbowStack)
                    ? SpellWeaponAmmo.createSyntheticArrow()
                    : ItemStack.EMPTY;
        }

        ItemStack storedStack = inventory.getStackInSlot(slot);
        ItemStack workingCopy = storedStack.copy();
        MAID_NOUVEAU_AMMO_USE.set(new MaidNouveauAmmoUse(inventory, slot, storedStack.getCount(), workingCopy));
        return workingCopy;
    }

    /**
     * Ars Nouveau only attaches mana to players.  A maid has no such pool, so its
     * spell crossbow would otherwise reject every completed charge and the maid AI
     * would start charging again.  Let the non-player caster proceed; Ars' mana
     * expenditure then remains a harmless no-op for the maid.
     */
    @Redirect(
            method = "tryLoadProjectiles",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hollingsworth/arsnouveau/api/spell/wrapped_caster/IWrappedCaster;enoughMana(I)Z"
            )
    )
    private boolean maidNouveau$allowMaidSpellCharge(IWrappedCaster caster, int cost) {
        if (caster instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster livingCaster
                && livingCaster.livingEntity instanceof EntityMaid) {
            return true;
        }
        return caster.enoughMana(cost);
    }

    @Inject(method = "buildSpellArrow", at = @At("RETURN"))
    private void maidNouveau$applyMaidArrowAndBowSpell(net.minecraft.world.level.Level level,
                                                       LivingEntity shooter,
                                                       AbstractCaster<?> caster,
                                                       ItemStack crossbowStack,
                                                       ItemStack arrowStack,
                                                       CallbackInfoReturnable<EntitySpellArrow> cir) {
        if (!(shooter instanceof EntityMaid) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Spell spell = caster.modifySpellBeforeCasting(
                serverLevel, shooter, InteractionHand.MAIN_HAND, caster.getSpell());
        if (arrowStack.getItem() instanceof SpellArrow spellArrowItem) {
            Spell.Mutable modifiedSpell = spell.mutable();
            spellArrowItem.modifySpell(modifiedSpell);
            spell = modifiedSpell.immutable();
        }

        EntitySpellArrow arrow = cir.getReturnValue();
        arrow.setResolver(new EntitySpellResolver(new SpellContext(
                serverLevel, spell, shooter, LivingCaster.from(shooter), crossbowStack)).withSilent(true));

        if (SpellWeaponAmmo.isSyntheticArrow(arrowStack)) {
            arrow.setBaseDamage(0.0D);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
    }

    @Inject(method = "tryLoadProjectiles", at = @At("RETURN"))
    private void maidNouveau$consumeMaidProjectile(LivingEntity shooter, ItemStack crossbowStack,
                                                   CallbackInfoReturnable<Boolean> cir) {
        MaidNouveauAmmoUse ammoUse = MAID_NOUVEAU_AMMO_USE.get();
        MAID_NOUVEAU_AMMO_USE.remove();
        if (ammoUse == null) {
            return;
        }

        int consumed = ammoUse.initialCount() - ammoUse.workingCopy().getCount();
        if (consumed > 0) {
            ammoUse.inventory().extractItem(ammoUse.slot(), consumed, false);
        }
    }

    @Unique
    private record MaidNouveauAmmoUse(CombinedInvWrapper inventory, int slot, int initialCount,
                                      ItemStack workingCopy) {
    }
}
