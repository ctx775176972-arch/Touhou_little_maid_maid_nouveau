package com.touhoulittlemaid.maidnouveau.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import com.touhoulittlemaid.maidnouveau.automation.ApparatusBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class TaskEnchantingApparatus extends AbstractArsWorkstationTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MaidNouveau.MOD_ID, "enchanting_apparatus");
    @Override public ResourceLocation getUid() { return UID; }
    @Override public ItemStack getIcon() { return BlockRegistry.ENCHANTING_APP_BLOCK.get().asItem().getDefaultInstance(); }
    @Override public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return new ArrayList<>(List.of(Pair.of(5, new ApparatusBehavior())));
    }
    @Override public String getMaidActionSummary() { return "Craft with a bound or nearby Enchanting Apparatus"; }
}
