package com.dronemod.network;

import com.dronemod.DroneMod;
import com.dronemod.entity.DroneEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class ModNetworking {
    public static final Identifier DRONE_CONTROL_PACKET = DroneMod.id("drone_control");
    public static final Identifier DRONE_CONNECT_PACKET = DroneMod.id("drone_connect");
    public static final Identifier DRONE_DISCONNECT_PACKET = DroneMod.id("drone_disconnect");

    public static void registerServerPackets() {
        // Control input from client
        ServerPlayNetworking.registerGlobalReceiver(DRONE_CONTROL_PACKET, (server, player, handler, buf, responseSender) -> {
            UUID droneUUID = buf.readUuid();
            float forward = buf.readFloat();
            float strafe = buf.readFloat();
            float vertical = buf.readFloat();
            float yaw = buf.readFloat();

            server.execute(() -> {
                DroneEntity drone = findDrone(player, droneUUID);
                if (drone != null && drone.isControlled()) {
                    drone.applyControlInput(forward, strafe, vertical, yaw);
                }
            });
        });

        // Connect to drone
        ServerPlayNetworking.registerGlobalReceiver(DRONE_CONNECT_PACKET, (server, player, handler, buf, responseSender) -> {
            UUID droneUUID = buf.readUuid();

            server.execute(() -> {
                DroneEntity drone = findDrone(player, droneUUID);
                if (drone != null) {
                    double dist = player.squaredDistanceTo(drone);
                    if (dist <= 64 * 64) {
                        drone.setControllerUUID(player.getUuid());
                        drone.setControlled(true);
                        drone.setFlying(true);
                    }
                }
            });
        });

        // Disconnect from drone
        ServerPlayNetworking.registerGlobalReceiver(DRONE_DISCONNECT_PACKET, (server, player, handler, buf, responseSender) -> {
            UUID droneUUID = buf.readUuid();

            server.execute(() -> {
                DroneEntity drone = findDrone(player, droneUUID);
                if (drone != null) {
                    drone.setControlled(false);
                    drone.setControllerUUID(null);
                }
            });
        });
    }

    public static void registerClientPackets() {
        // Nothing to receive on client for now
    }

    private static DroneEntity findDrone(ServerPlayerEntity player, UUID droneUUID) {
        for (Entity entity : player.getServerWorld().iterateEntities()) {
            if (entity instanceof DroneEntity drone && entity.getUuid().equals(droneUUID)) {
                return drone;
            }
        }
        return null;
    }

    // Client-side send methods
    public static void sendControlInput(UUID droneUUID, float forward, float strafe, float vertical, float yaw) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(droneUUID);
        buf.writeFloat(forward);
        buf.writeFloat(strafe);
        buf.writeFloat(vertical);
        buf.writeFloat(yaw);
        ClientPlayNetworking.send(DRONE_CONTROL_PACKET, buf);
    }

    public static void sendConnect(UUID droneUUID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(droneUUID);
        ClientPlayNetworking.send(DRONE_CONNECT_PACKET, buf);
    }

    public static void sendDisconnect(UUID droneUUID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(droneUUID);
        ClientPlayNetworking.send(DRONE_DISCONNECT_PACKET, buf);
    }
}
