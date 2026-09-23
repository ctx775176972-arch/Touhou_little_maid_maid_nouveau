package com.touhoulittlemaid.maidnouveau.client;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.touhoulittlemaid.maidnouveau.automation.AutomationSettings;
import com.touhoulittlemaid.maidnouveau.automation.WorkstationType;
import com.touhoulittlemaid.maidnouveau.menu.AutomationConfigMenu;
import com.touhoulittlemaid.maidnouveau.network.SetAutomationConfigPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AutomationConfigScreen extends MaidTaskConfigGui<AutomationConfigMenu> {
    private Button enabledButton;
    private EditBox secondsBox;

    public AutomationConfigScreen(AutomationConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        enabledButton = addRenderableWidget(Button.builder(enabledLabel(), button -> {
            menu.setCooldownEnabled(!menu.cooldownEnabled());
            button.setMessage(enabledLabel());
        }).bounds(leftPos + 90, topPos + 36, 150, 20).build());

        secondsBox = new EditBox(font, leftPos + 90, topPos + 68, 150, 20,
                Component.translatable("gui.touhou_little_maid_maid_nouveau.cooldown_seconds"));
        secondsBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        secondsBox.setMaxLength(4);
        secondsBox.setValue(Integer.toString(Math.max(AutomationSettings.MIN_SECONDS, menu.cooldownSeconds())));
        secondsBox.setHint(Component.translatable("gui.touhou_little_maid_maid_nouveau.cooldown_range"));
        addRenderableWidget(secondsBox);
    }

    private Component enabledLabel() {
        return Component.translatable(menu.cooldownEnabled()
                ? "gui.touhou_little_maid_maid_nouveau.cooldown_enabled"
                : "gui.touhou_little_maid_maid_nouveau.cooldown_disabled");
    }

    @Override
    public void onClose() {
        int seconds;
        try {
            seconds = Integer.parseInt(secondsBox.getValue());
        } catch (NumberFormatException ignored) {
            seconds = AutomationSettings.DEFAULT_SECONDS;
        }
        seconds = Mth.clamp(seconds, AutomationSettings.MIN_SECONDS, AutomationSettings.MAX_SECONDS);
        menu.setCooldownSeconds(seconds);
        WorkstationType type = menu.workstationType();
        if (type != null) {
            PacketDistributor.sendToServer(new SetAutomationConfigPayload(
                    menu.getMaid().getId(), type.id(), menu.cooldownEnabled(), seconds));
        }
        super.onClose();
    }
}
