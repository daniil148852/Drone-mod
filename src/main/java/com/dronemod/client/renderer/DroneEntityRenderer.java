package com.dronemod.client.renderer;

import com.dronemod.DroneMod;
import com.dronemod.client.model.DroneEntityModel;
import com.dronemod.entity.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class DroneEntityRenderer extends EntityRenderer<DroneEntity> {
    private static final Identifier TEXTURE = DroneMod.id("textures/entity/drone_entity.png");
    private final DroneEntityModel model;

    public DroneEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new DroneEntityModel(DroneEntityModel.getTexturedModelData().createModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(DroneEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Position adjustment
        matrices.translate(0, -1.2, 0);

        // Apply yaw rotation
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDeg(-entity.getYaw()));

        // Slight hover animation
        if (entity.isFlying()) {
            float hoverOffset = (float) Math.sin((entity.age + tickDelta) * 0.1) * 0.03f;
            matrices.translate(0, hoverOffset, 0);
        }

        // Tilt based on velocity
        float vx = (float) entity.getVelocity().x;
        float vz = (float) entity.getVelocity().z;
        float tiltX = -vz * 15f;
        float tiltZ = vx * 15f;
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDeg(tiltX));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDeg(tiltZ));

        // Set model angles
        model.setAngles(entity, 0, 0, entity.age + tickDelta, yaw, 0);

        // Render
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return TEXTURE;
    }
}
