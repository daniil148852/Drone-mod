package com.dronemod.registry;

import com.dronemod.DroneMod;
import com.dronemod.item.DroneItem;
import com.dronemod.item.TabletItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final Item DRONE_ITEM = new DroneItem(new FabricItemSettings().maxCount(1));
    public static final Item TABLET_ITEM = new TabletItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        Registry.register(Registries.ITEM, DroneMod.id("drone_item"), DRONE_ITEM);
        Registry.register(Registries.ITEM, DroneMod.id("tablet_item"), TABLET_ITEM);
    }
}
