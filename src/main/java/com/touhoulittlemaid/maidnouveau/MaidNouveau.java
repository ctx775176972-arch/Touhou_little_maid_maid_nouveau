package com.touhoulittlemaid.maidnouveau;

import com.mojang.logging.LogUtils;
import com.touhoulittlemaid.maidnouveau.event.MaidArsCombatEvents;
import com.touhoulittlemaid.maidnouveau.event.MaidAttributeEvents;
import com.touhoulittlemaid.maidnouveau.event.MaidManaRegenRepairEvents;
import com.touhoulittlemaid.maidnouveau.registry.MaidNouveauItems;
import com.touhoulittlemaid.maidnouveau.config.MaidNouveauConfig;
import com.touhoulittlemaid.maidnouveau.menu.MaidNouveauMenus;
import com.touhoulittlemaid.maidnouveau.network.SetAutomationConfigPayload;
import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(MaidNouveau.MOD_ID)
public final class MaidNouveau {
    public static final String MOD_ID = "touhou_little_maid_maid_nouveau";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidNouveau(IEventBus modEventBus, ModContainer modContainer) {
        MaidNouveauItems.ITEMS.register(modEventBus);
        MaidNouveauMenus.MENUS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MaidNouveauConfig.SPEC);
        modEventBus.addListener(MaidAttributeEvents::addArsAttributes);
        modEventBus.addListener(MaidNouveau::addCreativeTabItems);
        modEventBus.addListener(MaidNouveau::registerPayloads);

        NeoForge.EVENT_BUS.addListener(MaidArsCombatEvents::onShieldBlock);
        NeoForge.EVENT_BUS.addListener(MaidArsCombatEvents::onSpellDamage);
        NeoForge.EVENT_BUS.addListener(MaidManaRegenRepairEvents::onMaidTick);

        LOGGER.info("Loading Touhou Little Maid: Maid Nouveau");
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                SetAutomationConfigPayload.TYPE,
                SetAutomationConfigPayload.STREAM_CODEC,
                SetAutomationConfigPayload::handle
        );
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(InitCreativeTabs.MAIN_TAB.getKey())) {
            event.accept(MaidNouveauItems.WORKSTATION_MANAGER.get());
        }
    }
}
