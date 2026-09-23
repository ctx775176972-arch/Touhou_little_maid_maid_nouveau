package com.touhoulittlemaid.maidnouveau;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.touhoulittlemaid.maidnouveau.task.TaskEnchantingApparatus;
import com.touhoulittlemaid.maidnouveau.task.TaskPotionJar;
import com.touhoulittlemaid.maidnouveau.task.TaskRitualBrazier;

@LittleMaidExtension
public class MaidNouveauExtension implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskEnchantingApparatus());
        manager.add(new TaskPotionJar());
        manager.add(new TaskRitualBrazier());
    }
}
