package com.dronemod.registry;

import com.dronemod.DroneMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier DRONE_IDLE_ID = DroneMod.id("drone_idle");
    public static final Identifier DRONE_FLY_ID = DroneMod.id("drone_fly");
    public static final Identifier DRONE_CRASH_ID = DroneMod.id("drone_crash");

    public static final SoundEvent DRONE_IDLE = SoundEvent.of(DRONE_IDLE_ID);
    public static final SoundEvent DRONE_FLY = SoundEvent.of(DRONE_FLY_ID);
    public static final SoundEvent DRONE_CRASH = SoundEvent.of(DRONE_CRASH_ID);

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, DRONE_IDLE_ID, DRONE_IDLE);
        Registry.register(Registries.SOUND_EVENT, DRONE_FLY_ID, DRONE_FLY);
        Registry.register(Registries.SOUND_EVENT, DRONE_CRASH_ID, DRONE_CRASH);
    }
}
