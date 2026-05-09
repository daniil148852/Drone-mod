package com.dronemod;

import com.dronemod.client.renderer.DroneEntityRenderer;
import com.dronemod.network.ModNetworking;
import com.dronemod.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class DroneModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DRONE, DroneEntityRenderer::new);
        ModNetworking.registerClientPackets();
    }
}
