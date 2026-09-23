package com.touhoulittlemaid.maidnouveau.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import com.touhoulittlemaid.maidnouveau.automation.AutomationSettings;
import com.touhoulittlemaid.maidnouveau.automation.WorkstationType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetAutomationConfigPayload(int maidId, String workstationType, boolean enabled, int seconds)
        implements CustomPacketPayload {
    public static final Type<SetAutomationConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MaidNouveau.MOD_ID, "set_automation_config"));
    public static final StreamCodec<ByteBuf, SetAutomationConfigPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetAutomationConfigPayload::maidId,
            ByteBufCodecs.STRING_UTF8, SetAutomationConfigPayload::workstationType,
            ByteBufCodecs.BOOL, SetAutomationConfigPayload::enabled,
            ByteBufCodecs.VAR_INT, SetAutomationConfigPayload::seconds,
            SetAutomationConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetAutomationConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level().getEntity(payload.maidId()) instanceof EntityMaid maid)
                    || !maid.isOwnedBy(player)) {
                return;
            }
            WorkstationType type = WorkstationType.fromId(payload.workstationType());
            if (type != null) {
                AutomationSettings.set(maid, type, payload.enabled(), payload.seconds());
            }
        });
    }
}
