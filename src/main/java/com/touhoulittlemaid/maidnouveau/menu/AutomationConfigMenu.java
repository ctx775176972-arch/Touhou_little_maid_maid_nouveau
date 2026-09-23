package com.touhoulittlemaid.maidnouveau.menu;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import com.touhoulittlemaid.maidnouveau.automation.AutomationSettings;
import com.touhoulittlemaid.maidnouveau.automation.WorkstationType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;

public class AutomationConfigMenu extends TaskConfigContainer {
    private final SimpleContainerData settings = new SimpleContainerData(2);
    private final WorkstationType workstationType;

    public AutomationConfigMenu(int id, Inventory inventory, int maidId) {
        super(MaidNouveauMenus.AUTOMATION_CONFIG.get(), id, inventory, maidId);
        this.workstationType = typeFromMaid(maid);
        if (!inventory.player.level().isClientSide && workstationType != null) {
            settings.set(0, AutomationSettings.enabled(maid, workstationType) ? 1 : 0);
            settings.set(1, AutomationSettings.seconds(maid, workstationType));
        }
        addDataSlots(settings);
    }

    public boolean cooldownEnabled() {
        return settings.get(0) != 0;
    }

    public void setCooldownEnabled(boolean enabled) {
        settings.set(0, enabled ? 1 : 0);
    }

    public int cooldownSeconds() {
        return settings.get(1);
    }

    public void setCooldownSeconds(int seconds) {
        settings.set(1, seconds);
    }

    public WorkstationType workstationType() {
        return workstationType;
    }

    private static WorkstationType typeFromMaid(EntityMaid maid) {
        String path = maid.getTask().getUid().getPath();
        return switch (path) {
            case "enchanting_apparatus" -> WorkstationType.APPARATUS;
            case "potion_jar" -> WorkstationType.POTION_JAR;
            case "ritual_brazier" -> WorkstationType.RITUAL_BRAZIER;
            default -> null;
        };
    }
}
