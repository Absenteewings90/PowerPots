package com.leo.powerpots.network;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.entity.PowerPotBE;
import com.leo.powerpots.screen.UpgradeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider; // ← fix this import
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ModNetworking {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PowerPots.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
                0,
                OpenUpgradeGuiPacket.class,
                OpenUpgradeGuiPacket::encode,
                OpenUpgradeGuiPacket::decode,
                OpenUpgradeGuiPacket::handle  // ← Forge 1.20.1 accepts this signature directly
        );
    }

    public static void sendOpenUpgradeGui(BlockPos pos) {
        CHANNEL.sendToServer(new OpenUpgradeGuiPacket(pos));
    }

    public static class OpenUpgradeGuiPacket {
        private final BlockPos pos;

        public OpenUpgradeGuiPacket(BlockPos pos) {
            this.pos = pos;
        }

        public static void encode(OpenUpgradeGuiPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos);
        }

        public static OpenUpgradeGuiPacket decode(FriendlyByteBuf buf) {
            return new OpenUpgradeGuiPacket(buf.readBlockPos());
        }

        // ← fix: takes Supplier<Context> not Context directly
        public static void handle(OpenUpgradeGuiPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player == null) return;

                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof PowerPotBE powerPotBE) {
                    if (player.distanceToSqr(
                            packet.pos.getX() + 0.5,
                            packet.pos.getY() + 0.5,
                            packet.pos.getZ() + 0.5) <= 64.0) {
                        NetworkHooks.openScreen(
                                player,
                                new SimpleMenuProvider(
                                        (id, inv, p) -> new UpgradeMenu(id, inv, powerPotBE),
                                        Component.translatable("screen.powerpots.upgrades")
                                ),
                                buf -> buf.writeBlockPos(packet.pos)
                        );
                    }
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}