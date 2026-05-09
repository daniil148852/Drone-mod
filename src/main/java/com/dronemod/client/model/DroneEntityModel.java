package com.dronemod.client.model;

import com.dronemod.entity.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class DroneEntityModel extends EntityModel<DroneEntity> {
    private final ModelPart body;
    private final ModelPart armFL;
    private final ModelPart armFR;
    private final ModelPart armBL;
    private final ModelPart armBR;
    private final ModelPart propFL;
    private final ModelPart propFR;
    private final ModelPart propBL;
    private final ModelPart propBR;
    private final ModelPart camera;
    private final ModelPart landingGearL;
    private final ModelPart landingGearR;
    private final ModelPart ledGreen;
    private final ModelPart ledRed;

    public DroneEntityModel(ModelPart root) {
        this.body = root.getChild("body");
        this.armFL = root.getChild("arm_fl");
        this.armFR = root.getChild("arm_fr");
        this.armBL = root.getChild("arm_bl");
        this.armBR = root.getChild("arm_br");
        this.propFL = root.getChild("prop_fl");
        this.propFR = root.getChild("prop_fr");
        this.propBL = root.getChild("prop_bl");
        this.propBR = root.getChild("prop_br");
        this.camera = root.getChild("camera");
        this.landingGearL = root.getChild("gear_l");
        this.landingGearR = root.getChild("gear_r");
        this.ledGreen = root.getChild("led_green");
        this.ledRed = root.getChild("led_red");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Body - central frame (6x2x6 pixels, centered)
        root.addChild("body", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-3f, -1f, -3f, 6, 2, 6),
                ModelTransform.pivot(0, 21, 0));

        // Arms - diagonal beams to motors
        root.addChild("arm_fl", ModelPartBuilder.create()
                        .uv(0, 8)
                        .cuboid(-4f, -0.5f, -4f, 4, 1, 4),
                ModelTransform.pivot(-1, 21, -1));

        root.addChild("arm_fr", ModelPartBuilder.create()
                        .uv(0, 13)
                        .cuboid(0f, -0.5f, -4f, 4, 1, 4),
                ModelTransform.pivot(1, 21, -1));

        root.addChild("arm_bl", ModelPartBuilder.create()
                        .uv(16, 8)
                        .cuboid(-4f, -0.5f, 0f, 4, 1, 4),
                ModelTransform.pivot(-1, 21, 1));

        root.addChild("arm_br", ModelPartBuilder.create()
                        .uv(16, 13)
                        .cuboid(0f, -0.5f, 0f, 4, 1, 4),
                ModelTransform.pivot(1, 21, 1));

        // Propellers (thin discs) - 6x0.5x6
        root.addChild("prop_fl", ModelPartBuilder.create()
                        .uv(32, 0)
                        .cuboid(-3f, 0f, -3f, 6, 0, 6),
                ModelTransform.pivot(-5, 19.5f, -5));

        root.addChild("prop_fr", ModelPartBuilder.create()
                        .uv(32, 6)
                        .cuboid(-3f, 0f, -3f, 6, 0, 6),
                ModelTransform.pivot(5, 19.5f, -5));

        root.addChild("prop_bl", ModelPartBuilder.create()
                        .uv(44, 0)
                        .cuboid(-3f, 0f, -3f, 6, 0, 6),
                ModelTransform.pivot(-5, 19.5f, 5));

        root.addChild("prop_br", ModelPartBuilder.create()
                        .uv(44, 6)
                        .cuboid(-3f, 0f, -3f, 6, 0, 6),
                ModelTransform.pivot(5, 19.5f, 5));

        // Camera
        root.addChild("camera", ModelPartBuilder.create()
                        .uv(0, 18)
                        .cuboid(-1f, 0f, -1f, 2, 1, 2),
                ModelTransform.pivot(0, 22, -3));

        // Landing gear
        root.addChild("gear_l", ModelPartBuilder.create()
                        .uv(8, 18)
                        .cuboid(-0.5f, 0f, -2f, 1, 2, 4),
                ModelTransform.pivot(-3, 22, 0));

        root.addChild("gear_r", ModelPartBuilder.create()
                        .uv(18, 18)
                        .cuboid(-0.5f, 0f, -2f, 1, 2, 4),
                ModelTransform.pivot(3, 22, 0));

        // LEDs
        root.addChild("led_green", ModelPartBuilder.create()
                        .uv(0, 24)
                        .cuboid(-0.5f, -0.5f, -0.5f, 1, 1, 1),
                ModelTransform.pivot(-1, 19.5f, 0));

        root.addChild("led_red", ModelPartBuilder.create()
                        .uv(4, 24)
                        .cuboid(-0.5f, -0.5f, -0.5f, 1, 1, 1),
                ModelTransform.pivot(1, 19.5f, 0));

        return TexturedModelData.of(modelData, 64, 32);
    }

    @Override
    public void setAngles(DroneEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float rotation = (float) Math.toRadians(entity.getPropellerRotation());

        // Rotate propellers around Y axis
        propFL.yaw = rotation;
        propFR.yaw = -rotation;   // Counter-rotate
        propBL.yaw = -rotation;
        propBR.yaw = rotation;

        // Tilt body slightly based on movement
        if (entity.isControlled()) {
            float vx = (float) entity.getVelocity().x;
            float vz = (float) entity.getVelocity().z;

            body.pitch = vz * 0.5f;
            body.roll = -vx * 0.5f;
        } else {
            body.pitch *= 0.9f;
            body.roll *= 0.9f;
        }

        // LED blinking
        boolean ledOn = (animationProgress % 20) < 10;
        ledGreen.visible = entity.isControlled() || ledOn;
        ledRed.visible = !entity.isControlled() && ledOn;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        armFL.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        armFR.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        armBL.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        armBR.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        propFL.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        propFR.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        propBL.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        propBR.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        camera.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        landingGearL.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        landingGearR.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        ledGreen.render(matrices, vertexConsumer, light, overlay, 0, 1, 0, alpha);  // Green tint
        ledRed.render(matrices, vertexConsumer, light, overlay, 1, 0, 0, alpha);    // Red tint
    }
}
