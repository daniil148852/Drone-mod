package com.dronemod.entity;

import com.dronemod.registry.ModItems;
import com.dronemod.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class DroneEntity extends MobEntity implements Inventory {
    // Tracked data
    private static final TrackedData<Boolean> FLYING = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CONTROLLED = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> TARGET_YAW = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // Inventory
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    // Control state
    private UUID controllerUUID = null;
    private Vec3d targetVelocity = Vec3d.ZERO;
    private float propellerRotation = 0;
    private float propellerSpeed = 0;
    private int soundCooldown = 0;
    private double lastSafeY = 0;
    private int noControlTicks = 0;
    private boolean wasControlled = false;

    // Movement inertia
    private static final double ACCELERATION = 0.04;
    private static final double DECELERATION = 0.92;
    private static final double MAX_SPEED = 0.5;
    private static final double GRAVITY = 0.04;
    private static final double LANDING_SPEED = 0.03;
    private static final int MAX_CONTROL_RANGE = 64;
    private static final int FALL_DAMAGE_THRESHOLD = 10;

    public DroneEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setPersistent();
        this.setAiDisabled(true);
    }

    public static DefaultAttributeContainer.Builder createDroneAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FLYING, false);
        this.dataTracker.startTracking(CONTROLLED, false);
        this.dataTracker.startTracking(TARGET_YAW, 0f);
    }

    // Getters / Setters
    public boolean isFlying() { return this.dataTracker.get(FLYING); }
    public void setFlying(boolean flying) { this.dataTracker.set(FLYING, flying); }
    public boolean isControlled() { return this.dataTracker.get(CONTROLLED); }
    public void setControlled(boolean controlled) { this.dataTracker.set(CONTROLLED, controlled); }
    public float getTargetYaw() { return this.dataTracker.get(TARGET_YAW); }
    public void setTargetYaw(float yaw) { this.dataTracker.set(TARGET_YAW, yaw); }

    public UUID getControllerUUID() { return controllerUUID; }
    public void setControllerUUID(UUID uuid) { this.controllerUUID = uuid; }

    public float getPropellerRotation() { return propellerRotation; }
    public float getPropellerSpeed() { return propellerSpeed; }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            serverTick();
        } else {
            clientTick();
        }
    }

    private void serverTick() {
        boolean controlled = isControlled();
        Vec3d vel = this.getVelocity();

        if (controlled) {
            noControlTicks = 0;
            lastSafeY = this.getY();
            setFlying(true);

            // Apply target velocity with inertia
            double newVx = vel.x * DECELERATION + targetVelocity.x * ACCELERATION;
            double newVy = vel.y * DECELERATION + targetVelocity.y * ACCELERATION;
            double newVz = vel.z * DECELERATION + targetVelocity.z * ACCELERATION;

            // Clamp speed
            Vec3d newVel = new Vec3d(
                    clamp(newVx, -MAX_SPEED, MAX_SPEED),
                    clamp(newVy, -MAX_SPEED * 0.7, MAX_SPEED * 0.7),
                    clamp(newVz, -MAX_SPEED, MAX_SPEED)
            );
            this.setVelocity(newVel);

            // Check controller distance
            if (controllerUUID != null) {
                PlayerEntity controller = this.getWorld().getPlayerByUuid(controllerUUID);
                if (controller == null || controller.squaredDistanceTo(this) > MAX_CONTROL_RANGE * MAX_CONTROL_RANGE) {
                    setControlled(false);
                }
            }
        } else {
            noControlTicks++;
            if (isFlying()) {
                // Auto-landing: slow descent
                double newVx = vel.x * 0.95;
                double newVy = -LANDING_SPEED;
                double newVz = vel.z * 0.95;
                this.setVelocity(newVx, newVy, newVz);

                // Check if on ground
                if (this.isOnGround()) {
                    setFlying(false);
                    this.setVelocity(Vec3d.ZERO);

                    // Check fall damage
                    double fallDistance = lastSafeY - this.getY();
                    if (fallDistance > FALL_DAMAGE_THRESHOLD) {
                        crashDrone();
                    }
                }
            } else {
                // On ground, apply some gravity to stick
                this.setVelocity(vel.x * 0.5, -GRAVITY, vel.z * 0.5);
                if (this.isOnGround()) {
                    this.setVelocity(Vec3d.ZERO);
                }
            }
        }

        this.velocityModified = true;

        // Sound
        soundCooldown--;
        if (soundCooldown <= 0) {
            if (isFlying() && isControlled()) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.DRONE_FLY, SoundCategory.NEUTRAL, 0.6f, 0.9f + random.nextFloat() * 0.2f);
                soundCooldown = 40;
            } else if (isFlying()) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.DRONE_IDLE, SoundCategory.NEUTRAL, 0.3f, 1.0f);
                soundCooldown = 60;
            }
        }
    }

    private void clientTick() {
        // Propeller animation
        if (isFlying() || isControlled()) {
            float targetSpeed = isControlled() ? 30f : 10f;
            propellerSpeed += (targetSpeed - propellerSpeed) * 0.1f;
        } else {
            propellerSpeed *= 0.95f;
        }
        propellerRotation += propellerSpeed;
        if (propellerRotation > 360) propellerRotation -= 360;
    }

    public void applyControlInput(float forward, float strafe, float vertical, float yaw) {
        if (!isControlled()) return;

        double yawRad = Math.toRadians(yaw);
        double moveX = -Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe;
        double moveZ = Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe;

        this.targetVelocity = new Vec3d(moveX, vertical * 0.7, moveZ);
        this.setTargetYaw(yaw);
    }

    private void crashDrone() {
        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.DRONE_CRASH, SoundCategory.NEUTRAL, 1.0f, 1.0f);

            // Drop inventory
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty()) {
                    this.dropStack(stack);
                    inventory.set(i, ItemStack.EMPTY);
                }
            }

            // Drop drone item
            this.dropStack(new ItemStack(ModItems.DRONE_ITEM));
            this.discard();
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (held.getItem() == ModItems.TABLET_ITEM) {
                // Link tablet to this drone
                NbtCompound nbt = held.getOrCreateNbt();
                nbt.putUuid("DroneUUID", this.getUuid());
                nbt.putString("DroneName", this.getName().getString());
                player.sendMessage(net.minecraft.text.Text.literal("§aDrone linked to tablet!"), true);
                return ActionResult.SUCCESS;
            } else if (player.isSneaking()) {
                // Open inventory (sneak + right click)
                // Simple: drop all items
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.get(i);
                    if (!stack.isEmpty()) {
                        player.getInventory().offerOrDrop(stack);
                        inventory.set(i, ItemStack.EMPTY);
                    }
                }
                return ActionResult.SUCCESS;
            } else if (!held.isEmpty()) {
                // Put item in drone inventory
                for (int i = 0; i < inventory.size(); i++) {
                    if (inventory.get(i).isEmpty()) {
                        inventory.set(i, held.copy());
                        held.setCount(0);
                        player.sendMessage(net.minecraft.text.Text.literal("§eItem stored in drone"), true);
                        return ActionResult.SUCCESS;
                    }
                }
                player.sendMessage(net.minecraft.text.Text.literal("§cDrone inventory full!"), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (!this.getWorld().isClient && !this.isRemoved()) {
            if (amount > 5) {
                crashDrone();
                return true;
            }
            return super.damage(source, amount);
        }
        return false;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        // Drop inventory on death
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                this.dropStack(stack);
            }
        }
        this.dropStack(new ItemStack(ModItems.DRONE_ITEM));
        super.onDeath(damageSource);
    }

    // NBT
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Flying", isFlying());
        nbt.putBoolean("Controlled", isControlled());
        if (controllerUUID != null) {
            nbt.putUuid("ControllerUUID", controllerUUID);
        }
        Inventories.writeNbt(nbt, inventory);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setFlying(nbt.getBoolean("Flying"));
        setControlled(false); // Always start uncontrolled on load
        if (nbt.containsUuid("ControllerUUID")) {
            controllerUUID = nbt.getUuid("ControllerUUID");
        }
        Inventories.readNbt(nbt, inventory);
    }

    // Inventory interface
    @Override public int size() { return 9; }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { return Inventories.splitStack(inventory, slot, amount); }
    @Override public ItemStack removeStack(int slot) { return Inventories.removeStack(inventory, slot); }
    @Override public void setStack(int slot, ItemStack stack) { inventory.set(slot, stack); }
    @Override public void markDirty() {}
    @Override public boolean canPlayerUse(PlayerEntity player) { return player.squaredDistanceTo(this) < 64; }
    @Override public void clear() { inventory.clear(); }

    // Prevent other AI
    @Override public boolean canMoveVoluntarily() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void pushAway(net.minecraft.entity.Entity entity) {}

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public boolean isCollidable() { return true; }
}
