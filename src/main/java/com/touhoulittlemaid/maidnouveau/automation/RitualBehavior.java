package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.ANFakePlayer;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import com.hollingsworth.arsnouveau.common.items.RitualTablet;
import com.touhoulittlemaid.maidnouveau.config.MaidNouveauConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class RitualBehavior extends AbstractWorkstationBehavior<RitualBrazierTile> {
    public RitualBehavior() {
        super(WorkstationType.RITUAL_BRAZIER, MaidNouveauConfig.RITUAL_WORK_INTERVAL);
    }

    @Override
    protected WorkResult performWork(ServerLevel level, EntityMaid maid, RitualBrazierTile brazier) {
        if (brazier.ritual == null) {
            ItemStack held = maid.getMainHandItem();
            if (!(held.getItem() instanceof RitualTablet tablet)) {
                return WorkResult.NONE;
            }
            brazier.setRitual(tablet.ritual.getRegistryName());
            if (brazier.ritual == null) {
                return WorkResult.NONE;
            }
            held.shrink(1);
            maid.setItemSlot(EquipmentSlot.MAINHAND, held);
            return WorkResult.PROGRESS;
        }
        if (brazier.ritual.isRunning() || brazier.isRitualDone()) {
            return WorkResult.NONE;
        }

        IItemHandler inventory = maid.getAvailableBackpackInv();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && brazier.tryBurnStack(stack)) {
                return WorkResult.PROGRESS;
            }
        }

        Player fakePlayer = ANFakePlayer.getPlayer(level, maid.getUUID());
        fakePlayer.setPos(maid.getX(), maid.getY(), maid.getZ());
        boolean wasRunning = brazier.ritual.isRunning();
        brazier.startRitual(fakePlayer);
        return !wasRunning && brazier.ritual.isRunning() ? WorkResult.COMPLETE : WorkResult.NONE;
    }
}
