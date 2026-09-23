package com.touhoulittlemaid.maidnouveau.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskBowAttack;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractCaster;
import com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntitySpellArrow;
import com.hollingsworth.arsnouveau.common.items.FormSpellArrow;
import com.hollingsworth.arsnouveau.common.items.SpellArrow;
import com.hollingsworth.arsnouveau.common.items.SpellBow;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSplit;
import com.touhoulittlemaid.maidnouveau.util.SpellWeaponAmmo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the scribed Spell Bow spell to arrows created by the maid's existing bow task. */
@Mixin(value = TaskBowAttack.class, remap = false)
public abstract class MaidBowAttackMixin {
    @Unique
    private static final ThreadLocal<ItemStack> MAID_NOUVEAU_BOW_AMMO = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<MaidNouveauSplitShot> MAID_NOUVEAU_SPLIT_SHOT = new ThreadLocal<>();

    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    private void maidNouveau$clearSplitShot(EntityMaid maid, LivingEntity target, float power,
                                             CallbackInfo ci) {
        MAID_NOUVEAU_SPLIT_SHOT.remove();
    }

    @Inject(method = "getArrow", at = @At("HEAD"))
    private void maidNouveau$clearBowAmmo(EntityMaid maid, float power,
                                           CallbackInfoReturnable<AbstractArrow> cir) {
        MAID_NOUVEAU_BOW_AMMO.remove();
    }

    @ModifyArg(
            method = "getArrow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getMobArrow(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;FLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;",
                    remap = true
            ),
            index = 1
    )
    private ItemStack maidNouveau$captureActualBowAmmo(ItemStack arrowStack) {
        MAID_NOUVEAU_BOW_AMMO.set(arrowStack.copy());
        return arrowStack;
    }

    @Inject(method = "hasArrow", at = @At("RETURN"), cancellable = true)
    private void maidNouveau$allowScribedSpellBowWithoutArrows(EntityMaid maid,
                                                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()
                && maid.getMainHandItem().getItem() instanceof SpellBow
                && SpellWeaponAmmo.hasValidSpell(maid.getMainHandItem())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getArrow", at = @At("RETURN"), cancellable = true)
    private void maidNouveau$attachSpellBowSpell(EntityMaid maid, float power,
                                                  CallbackInfoReturnable<AbstractArrow> cir) {
        AbstractArrow originalArrow = cir.getReturnValue();
        ItemStack actualAmmo = MAID_NOUVEAU_BOW_AMMO.get();
        MAID_NOUVEAU_BOW_AMMO.remove();
        ItemStack bowStack = maid.getMainHandItem();
        if (!(bowStack.getItem() instanceof SpellBow)
                || !(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AbstractCaster<?> caster = SpellCasterRegistry.from(bowStack);
        if (caster == null || !caster.getSpell().isValid()) {
            return;
        }

        Spell spell = caster.modifySpellBeforeCasting(
                serverLevel, maid, InteractionHand.MAIN_HAND, caster.getSpell());
        boolean syntheticArrow = originalArrow == null;
        ItemStack arrowStack = syntheticArrow
                ? SpellWeaponAmmo.createSyntheticArrow()
                : actualAmmo == null ? originalArrow.getPickupItemStackOrigin().copy() : actualAmmo;
        if (arrowStack.getItem() instanceof SpellArrow spellArrow) {
            Spell.Mutable modifiedSpell = spell.mutable();
            spellArrow.modifySpell(modifiedSpell);
            spell = modifiedSpell.immutable();
        }

        EntitySpellArrow spellArrow = new EntitySpellArrow(serverLevel, maid, arrowStack, bowStack);
        spellArrow.setResolver(new EntitySpellResolver(new SpellContext(
                serverLevel, spell, maid, LivingCaster.from(maid), bowStack)).withSilent(true));

        if (syntheticArrow) {
            spellArrow.setBaseDamage(0.0D);
            spellArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            spellArrow.setBaseDamage(originalArrow.getBaseDamage());
            spellArrow.setCritArrow(originalArrow.isCritArrow());
            spellArrow.setRemainingFireTicks(originalArrow.getRemainingFireTicks());
            spellArrow.pierceLeft = originalArrow.getPierceLevel();
            spellArrow.pickup = originalArrow.pickup;
        }
        if (arrowStack.getItem() instanceof FormSpellArrow formArrow
                && formArrow.part == AugmentSplit.INSTANCE
                && formArrow.numParts > 0) {
            MAID_NOUVEAU_SPLIT_SHOT.set(new MaidNouveauSplitShot(
                    bowStack.copy(), arrowStack.copyWithCount(1), spell, spellArrow, formArrow.numParts));
        }
        cir.setReturnValue(spellArrow);
    }

    @Inject(method = "performRangedAttack", at = @At("RETURN"))
    private void maidNouveau$spawnSplitArrows(EntityMaid maid, LivingEntity target, float power,
                                               CallbackInfo ci) {
        MaidNouveauSplitShot shot = MAID_NOUVEAU_SPLIT_SHOT.get();
        MAID_NOUVEAU_SPLIT_SHOT.remove();
        if (shot == null || !(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double dx = target.getX() - maid.getX();
        double dy = target.getEyeY() - maid.getEyeY();
        double dz = target.getZ() - maid.getZ();
        float distance = maid.distanceTo(target);
        float velocity = Math.clamp(distance / 10.0F, 1.6F, 3.2F);
        float inaccuracy = 1.0F - Math.clamp(distance / 100.0F, 0.0F, 0.9F);
        Vec3 baseDirection = new Vec3(dx, dy, dz);

        for (int index = 0; index < shot.extraArrowCount(); index++) {
            int step = index / 2 + 1;
            float angle = step * 10.0F * (index % 2 == 0 ? 1.0F : -1.0F);
            Vec3 direction = baseDirection.yRot((float) Math.toRadians(angle));

            EntitySpellArrow extraArrow = new EntitySpellArrow(
                    serverLevel, maid, shot.arrowStack().copy(), shot.bowStack());
            extraArrow.setResolver(new EntitySpellResolver(new SpellContext(
                    serverLevel, shot.spell(), maid, LivingCaster.from(maid), shot.bowStack())).withSilent(true));
            extraArrow.setBaseDamage(shot.template().getBaseDamage());
            extraArrow.setCritArrow(shot.template().isCritArrow());
            extraArrow.setRemainingFireTicks(shot.template().getRemainingFireTicks());
            extraArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            extraArrow.setNoGravity(true);
            extraArrow.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);
            serverLevel.addFreshEntity(extraArrow);
        }
    }

    @Unique
    private record MaidNouveauSplitShot(ItemStack bowStack, ItemStack arrowStack, Spell spell,
                                         EntitySpellArrow template, int extraArrowCount) {
    }
}
