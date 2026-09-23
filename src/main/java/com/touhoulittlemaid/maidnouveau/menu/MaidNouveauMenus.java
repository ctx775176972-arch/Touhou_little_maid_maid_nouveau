package com.touhoulittlemaid.maidnouveau.menu;

import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MaidNouveauMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MaidNouveau.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<AutomationConfigMenu>> AUTOMATION_CONFIG = MENUS.register(
            "automation_config",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new AutomationConfigMenu(id, inventory, buffer.readInt()))
    );

    private MaidNouveauMenus() {
    }
}
