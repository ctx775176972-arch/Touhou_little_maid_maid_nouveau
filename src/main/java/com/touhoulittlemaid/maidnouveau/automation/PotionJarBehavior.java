package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.potion.IPotionProvider;
import com.hollingsworth.arsnouveau.api.registry.PotionProviderRegistry;
import com.hollingsworth.arsnouveau.common.block.tile.PotionJarTile;
import com.hollingsworth.arsnouveau.common.items.PotionFlask;
import com.hollingsworth.arsnouveau.common.items.data.MultiPotionContents;
import com.hollingsworth.arsnouveau.common.util.PotionUtil;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.touhoulittlemaid.maidnouveau.config.MaidNouveauConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class PotionJarBehavior extends AbstractWorkstationBehavior<PotionJarTile> {
    public PotionJarBehavior() {
        super(WorkstationType.POTION_JAR, MaidNouveauConfig.POTION_JAR_WORK_INTERVAL);
    }

    @Override
    protected WorkResult performWork(ServerLevel level, EntityMaid maid, PotionJarTile jar) {
        if (jar.getAmount() < 100 || jar.getData().equals(PotionContents.EMPTY)) {
            return WorkResult.NONE;
        }
        IItemHandler inventory = maid.getAvailableBackpackInv();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof PotionFlask) {
                IPotionProvider provider = PotionProviderRegistry.from(stack);
                if (provider == null) {
                    continue;
                }
                int charges = provider.usesRemaining(stack);
                int max = provider.maxUses(stack);
                PotionContents existing = provider.getPotionData(stack);
                if (charges < max && (charges == 0 || PotionUtil.arePotionContentsEqual(existing, jar.getData()))) {
                    stack.set(DataComponentRegistry.MULTI_POTION, new MultiPotionContents(charges + 1, jar.getData(), max));
                    jar.remove(100);
                    return WorkResult.COMPLETE;
                }
            }
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).is(Items.GLASS_BOTTLE)) {
                continue;
            }
            ItemStack bottle = inventory.extractItem(slot, 1, false);
            if (bottle.isEmpty()) {
                continue;
            }
            ItemStack potion = new ItemStack(Items.POTION);
            potion.set(DataComponents.POTION_CONTENTS, jar.getData());
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, potion, false);
            if (remainder.isEmpty()) {
                jar.remove(100);
                return WorkResult.COMPLETE;
            }
            ItemHandlerHelper.insertItemStacked(inventory, bottle, false);
        }
        return WorkResult.NONE;
    }
}
