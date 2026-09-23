package com.touhoulittlemaid.maidnouveau.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import com.touhoulittlemaid.maidnouveau.menu.AutomationConfigMenu;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractArsWorkstationTask implements IMaidTask {
    @Override
    public MenuProvider getTaskConfigGuiProvider(EntityMaid maid) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new AutomationConfigMenu(id, inventory, maid.getId()),
                Component.translatable("gui.touhou_little_maid_maid_nouveau.automation_config")
        );
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_IDLE.get(), 0.5F);
    }

    @Override
    public boolean workPointTask(EntityMaid maid) {
        return true;
    }
}
