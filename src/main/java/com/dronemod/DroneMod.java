package com.dronemod;

import com.dronemod.registry.ModEntities;
import com.dronemod.registry.ModItems;
import com.dronemod.registry.ModSounds;
import com.dronemod.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneMod implements ModInitializer {
    public static final String MOD_ID = "dronemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.DRONE_ITEM))
            .displayName(Text.translatable("itemGroup.dronemod.main"))
            .entries((context, entries) -> {
                entries.add(ModItems.DRONE_ITEM);
                entries.add(ModItems.TABLET_ITEM);
            })
            .build();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Drone Mod");
        ModItems.register();
        ModEntities.register();
        ModSounds.register();
        ModNetworking.registerServerPackets();

        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "main"), ITEM_GROUP);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
