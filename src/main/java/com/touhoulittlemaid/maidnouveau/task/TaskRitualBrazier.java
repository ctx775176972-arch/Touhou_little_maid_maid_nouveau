package com.touhoulittlemaid.maidnouveau.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import com.touhoulittlemaid.maidnouveau.automation.RitualBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class TaskRitualBrazier extends AbstractArsWorkstationTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MaidNouveau.MOD_ID, "ritual_brazier");
    @Override public ResourceLocation getUid() { return UID; }
    @Override public ItemStack getIcon() { return BlockRegistry.RITUAL_BLOCK.get().asItem().getDefaultInstance(); }
    @Override public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return new ArrayList<>(List.of(Pair.of(5, new RitualBehavior())));
    }
    @Override public String getMaidActionSummary() { return "Load and start a ritual at a bound or nearby Ritual Brazier"; }
}
