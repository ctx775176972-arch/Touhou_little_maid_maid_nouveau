package com.touhoulittlemaid.maidnouveau.registry;

import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import com.touhoulittlemaid.maidnouveau.item.WorkstationManagerItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MaidNouveauItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaidNouveau.MOD_ID);
    public static final DeferredItem<Item> WORKSTATION_MANAGER = ITEMS.registerItem(
            "workstation_manager", WorkstationManagerItem::new, new Item.Properties().stacksTo(1));

    private MaidNouveauItems() {
    }
}
