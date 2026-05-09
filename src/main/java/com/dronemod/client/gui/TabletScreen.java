package com.dronemod.client.gui;

import com.dronemod.DroneMod;
import com.dronemod.entity.DroneEntity;
import com.dronemod.network.ModNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TabletScreen extends Screen {
    private static final Identifier TEXTURE = DroneMod.id("textures/gui/tablet_gui.png");

    private final DroneEntity drone;
    private boolean connected = false;

    // Control state
    private boolean movingForward = false;
    private boolean movingBack = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private float controlYaw = 0;

    // GUI dimensions
    private int guiLeft;
    private int guiTop;
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;

    public TabletScreen(DroneEntity drone) {
        super(Text.translatable("gui.dronemod.tablet_title"));
        this.drone = drone;
        this.controlYaw = drone.getYaw();
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - GUI_WIDTH) / 2;
        guiTop = (this.height - GUI_HEIGHT) / 2;

        // Connect / Disconnect button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("CONNECT"), button -> {
            if (!connected) {
                ModNetworking.sendConnect(drone.getUuid());
                connected = true;
                button.setMessage(Text.literal("DISCONNECT"));
            } else {
                ModNetworking.sendDisconnect(drone.getUuid());
                connected = false;
                button.setMessage(Text.literal("CONNECT"));
            }
        }).dimensions(guiLeft + 10, guiTop + GUI_HEIGHT - 30, 80, 20).build());

        // W
        this.addDrawableChild(ButtonWidget.builder(Text.literal("W"), b -> {})
                .dimensions(guiLeft + 130, guiTop + 130, 25, 20).build());
        // A
        this.addDrawableChild(ButtonWidget.builder(Text.literal("A"), b -> {})
                .dimensions(guiLeft + 100, guiTop + 155, 25, 20).build());
        // S
        this.addDrawableChild(ButtonWidget.builder(Text.literal("S"), b -> {})
                .dimensions(guiLeft + 130, guiTop + 155, 25, 20).build());
        // D
        this.addDrawableChild(ButtonWidget.builder(Text.literal("D"), b -> {})
                .dimensions(guiLeft + 160, guiTop + 155, 25, 20).build());

        // UP
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.dronemod.up"), b -> {})
                .dimensions(guiLeft + 200, guiTop + 130, 40, 20).build());
        // DOWN
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.dronemod.down"), b -> {})
                .dimensions(guiLeft + 200, guiTop + 155, 40, 20).build());

        // Rotate Left
        this.addDrawableChild(ButtonWidget.builder(Text.literal("◄"), b -> {
            controlYaw -= 15;
        }).dimensions(guiLeft + 10, guiTop + 155, 25, 20).build());

        // Rotate Right
        this.addDrawableChild(ButtonWidget.builder(Text.literal("►"), b -> {
            controlYaw += 15;
        }).dimensions(guiLeft + 40, guiTop + 155, 25, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 87: movingForward = true; return true;  // W
            case 83: movingBack = true; return true;     // S
            case 65: movingLeft = true; return true;     // A
            case 68: movingRight = true; return true;    // D
            case 32: movingUp = true; return true;       // Space
            case 340: movingDown = true; return true;    // Left Shift
            case 81: controlYaw -= 10; return true;      // Q rotate left
            case 69: controlYaw += 10; return true;      // E rotate right
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 87: movingForward = false; return true;
            case 83: movingBack = false; return true;
            case 65: movingLeft = false; return true;
            case 68: movingRight = false; return true;
            case 32: movingUp = false; return true;
            case 340: movingDown = false; return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();

        if (connected && drone != null && !drone.isRemoved()) {
            float forward = 0, strafe = 0, vertical = 0;
            if (movingForward) forward += 1;
            if (movingBack) forward -= 1;
            if (movingLeft) strafe -= 1;
            if (movingRight) strafe += 1;
            if (movingUp) vertical += 1;
            if (movingDown) vertical -= 1;

            ModNetworking.sendControlInput(drone.getUuid(), forward, strafe, vertical, controlYaw);

            // Check distance
            if (client != null && client.player != null) {
                double dist = client.player.distanceTo(drone);
                if (dist > 64) {
                    connected = false;
                    ModNetworking.sendDisconnect(drone.getUuid());
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // Background panel
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xDD1E1E24);
        context.drawBorder(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF3C3C46);

        // Screen area (drone view)
        int screenX = guiLeft + 10;
        int screenY = guiTop + 10;
        int screenW = GUI_WIDTH - 20;
        int screenH = 110;
        context.fill(screenX, screenY, screenX + screenW, screenY + screenH, 0xFF0F281A);
        context.drawBorder(screenX, screenY, screenW, screenH, 0xFF286432);

        // Grid on screen
        for (int x = screenX; x < screenX + screenW; x += 20) {
            context.drawVerticalLine(x, screenY, screenY + screenH, 0x20286432);
        }
        for (int y = screenY; y < screenY + screenH; y += 20) {
            context.drawHorizontalLine(screenX, screenX + screenW, y, 0x20286432);
        }

        // Crosshair
        int cx = screenX + screenW / 2;
        int cy = screenY + screenH / 2;
        context.drawHorizontalLine(cx - 8, cx + 8, cy, 0xFF00FF00);
        context.drawVerticalLine(cx, cy - 8, cy + 8, 0xFF00FF00);

        if (drone != null && !drone.isRemoved()) {
            // Connection status
            if (connected) {
                context.drawText(textRenderer, "§a● " + Text.translatable("gui.dronemod.connected").getString(),
                        screenX + 5, screenY + 5, 0x00FF00, false);
            } else {
                context.drawText(textRenderer, "§c● " + Text.translatable("gui.dronemod.no_signal").getString(),
                        screenX + 5, screenY + 5, 0xFF0000, false);
            }

            // Distance
            double dist = 0;
            if (client != null && client.player != null) {
                dist = client.player.distanceTo(drone);
            }
            String distStr = Text.translatable("gui.dronemod.distance").getString() + ": " + String.format("%.1f", dist) + "m";
            context.drawText(textRenderer, distStr, screenX + 5, screenY + 18, 0x80FF80, false);

            // Altitude
            double altitude = drone.getY();
            String altStr = Text.translatable("gui.dronemod.altitude").getString() + ": " + String.format("%.1f", altitude);
            context.drawText(textRenderer, altStr, screenX + 5, screenY + 30, 0x80FF80, false);

            // Drone position
            String posStr = String.format("X:%.0f Y:%.0f Z:%.0f", drone.getX(), drone.getY(), drone.getZ());
            context.drawText(textRenderer, posStr, screenX + 5, screenY + 42, 0x6080FF, false);

            // Yaw indicator
            String yawStr = "Heading: " + String.format("%.0f°", controlYaw % 360);
            context.drawText(textRenderer, yawStr, screenX + 5, screenY + 54, 0x80FF80, false);

            // Speed indicator
            double speed = drone.getVelocity().length();
            String speedStr = "Speed: " + String.format("%.2f", speed);
            context.drawText(textRenderer, speedStr, screenX + 5, screenY + 66, 0x80FF80, false);

            // Drone direction compass on screen
            drawCompass(context, screenX + screenW - 30, screenY + 30, controlYaw);

            // Distance bar
            int barX = screenX + screenW - 15;
            int barH = screenH - 20;
            int barY = screenY + 10;
            context.fill(barX, barY, barX + 8, barY + barH, 0x40000000);

            // Fill based on distance ratio (0-64 blocks)
            float distRatio = (float) MathHelper.clamp(dist / 64.0, 0, 1);
            int fillH = (int)(barH * distRatio);
            int barColor = distRatio > 0.8 ? 0xFFFF0000 : (distRatio > 0.5 ? 0xFFFFFF00 : 0xFF00FF00);
            context.fill(barX, barY + barH - fillH, barX + 8, barY + barH, barColor);

            // Altitude bar (left side)
            int aBarX = screenX + 3;
            context.fill(aBarX, barY, aBarX + 8, barY + barH, 0x40000000);
            float altRatio = (float) MathHelper.clamp(altitude / 320.0, 0, 1);
            int altFillH = (int)(barH * altRatio);
            context.fill(aBarX, barY + barH - altFillH, aBarX + 8, barY + barH, 0xFF4488FF);

            // Inventory indicator
            int invCount = 0;
            for (int i = 0; i < drone.size(); i++) {
                if (!drone.getStack(i).isEmpty()) invCount++;
            }
            context.drawText(textRenderer, "Inv: " + invCount + "/9",
                    screenX + screenW - 60, screenY + screenH - 14, 0xAAAAAA, false);
        } else {
            context.drawText(textRenderer, "§c§lDRONE NOT FOUND", cx - 40, cy - 4, 0xFF0000, true);
        }

        // Control hints
        context.drawText(textRenderer, "§7WASD: Move | Space/Shift: Up/Down | Q/E: Rotate",
                guiLeft + 10, guiTop + GUI_HEIGHT - 12, 0x808080, false);

        // Title
        context.drawText(textRenderer, "§f§l" + this.title.getString(),
                guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(this.title) / 2,
                guiTop + GUI_HEIGHT - 45, 0xFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCompass(DrawContext context, int cx, int cy, float yaw) {
        int radius = 15;
        context.fill(cx - radius, cy - radius, cx + radius, cy + radius, 0x40000000);

        // Cardinal directions
        double rad = Math.toRadians(-yaw);
        int nx = cx + (int)(Math.sin(rad) * (radius - 4));
        int ny = cy - (int)(Math.cos(rad) * (radius - 4));
        context.drawText(textRenderer, "§cN", nx - 2, ny - 4, 0xFF0000, false);

        double sRad = rad + Math.PI;
        int sx = cx + (int)(Math.sin(sRad) * (radius - 4));
        int sy = cy - (int)(Math.cos(sRad) * (radius - 4));
        context.drawText(textRenderer, "§7S", sx - 2, sy - 4, 0x808080, false);

        // Center dot
        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF00FF00);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (connected && drone != null) {
            ModNetworking.sendDisconnect(drone.getUuid());
        }
        super.close();
    }
}
