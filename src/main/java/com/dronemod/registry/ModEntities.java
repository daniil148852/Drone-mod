package com.dronemod.registry;

import com.dronemod.DroneMod;
import com.dronemod.entity.DroneEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<DroneEntity> DRONE = Registry.register(
            Registries.ENTITY_TYPE,
            DroneMod.id("drone"),
            FabricEntityTypeBuilder.<DroneEntity>create(SpawnGroup.MISC, DroneEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 0.4f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(DRONE, DroneEntity.createDroneAttributes());
    }
}
