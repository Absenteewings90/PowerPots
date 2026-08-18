package com.leo.powerpots.network;

import com.leo.powerpots.block.entity.PowerPotBlockEntity;
import com.leo.powerpots.screen.UpgradeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenUpgradeGuiPacket(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("powerpots", "open_upgrade_gui");
    public static final Type<OpenUpgradeGuiPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenUpgradeGuiPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBlockPos(pkt.pos),
                    buf -> new OpenUpgradeGuiPacket(buf.readBlockPos())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenUpgradeGuiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(packet.pos());
                if (be instanceof PowerPotBlockEntity powerPotBE) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new UpgradeMenu(id, inv, powerPotBE),
                                    Component.translatable("screen.powerpots.upgrades")
                            ),
                            buf -> buf.writeBlockPos(packet.pos())
                    );
                }
            }
        });
    }
}
