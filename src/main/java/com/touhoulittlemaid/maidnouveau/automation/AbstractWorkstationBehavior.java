package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.touhoulittlemaid.maidnouveau.config.MaidNouveauConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Comparator;
import java.util.function.Supplier;

public abstract class AbstractWorkstationBehavior<T extends BlockEntity> extends MaidCheckRateTask {
    private final WorkstationType type;
    private final Supplier<Integer> interval;
    private BlockPos target;

    protected AbstractWorkstationBehavior(WorkstationType type, Supplier<Integer> interval) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.type = type;
        this.interval = interval;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        setMaxCheckRate(interval.get());
        if (!super.checkExtraStartConditions(level, maid) || !maid.canBrainMoving()
                || !AutomationSettings.ready(maid, type, level.getGameTime())) {
            return false;
        }
        target = findTarget(level, maid);
        if (target == null) {
            return false;
        }
        if (target.distToCenterSqr(maid.position()) > 6.25D) {
            BehaviorUtils.setWalkAndLookTargetMemories(maid, target, MaidNouveauConfig.MOVE_SPEED.get().floatValue(), 1);
            setNextCheckTickCount(5);
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockEntity blockEntity = target == null ? null : level.getBlockEntity(target);
        if (type.matches(blockEntity)) {
            @SuppressWarnings("unchecked") T station = (T) blockEntity;
            WorkResult result = performWork(level, maid, station);
            if (result != WorkResult.NONE) {
                maid.swing(InteractionHand.MAIN_HAND);
            }
            if (result == WorkResult.COMPLETE) {
                AutomationSettings.complete(maid, type, level.getGameTime());
            }
        }
        target = null;
    }

    protected abstract WorkResult performWork(ServerLevel level, EntityMaid maid, T station);

    private BlockPos findTarget(ServerLevel level, EntityMaid maid) {
        BlockPos bound = WorkstationBinding.get(maid, type)
                .filter(binding -> binding.dimension().equals(level.dimension()))
                .map(WorkstationBinding::pos)
                .filter(maid::isWithinRestriction)
                .filter(pos -> type.matches(level.getBlockEntity(pos)))
                .orElse(null);
        if (bound != null) {
            return bound;
        }

        BlockPos center = maid.getBrainSearchPos();
        int radius = MaidNouveauConfig.SEARCH_RADIUS.get();
        if (maid.hasRestriction()) {
            radius = Math.min(radius, Math.max(1, (int) maid.getRestrictRadius()));
        }
        return BlockPos.betweenClosedStream(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))
                .filter(maid::isWithinRestriction)
                .filter(pos -> type.matches(level.getBlockEntity(pos)))
                .map(BlockPos::immutable)
                .min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition())))
                .orElse(null);
    }
}
