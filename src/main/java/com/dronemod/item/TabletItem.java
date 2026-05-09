package com.dronemod.item;

import com.dronemod.client.gui.TabletScreen;
import com.dronemod.entity.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class TabletItem extends Item {
    public TabletItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.containsUuid("DroneUUID")) {
                UUID droneUUID = nbt.getUuid("DroneUUID");
                openTabletScreen(droneUUID);
            } else {
                player.sendMessage(Text.literal("§cTablet not linked! Right-click a drone to link."), true);
            }
        }
        return TypedActionResult.success(stack);
    }

    @Environment(EnvType.CLIENT)
    private void openTabletScreen(UUID droneUUID) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            // Find drone entity
            DroneEntity drone = null;
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof DroneEntity d && entity.getUuid().equals(droneUUID)) {
                    drone = d;
                    break;
                }
            }
            if (drone != null) {
                client.setScreen(new TabletScreen(drone));
            } else {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§cDrone not found nearby!"), true);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.containsUuid("DroneUUID")) {
            tooltip.add(Text.literal("§aLinked to drone").formatted(Formatting.GREEN));
        } else {
            tooltip.add(Text.translatable("tooltip.dronemod.tablet_not_linked").formatted(Formatting.RED));
        }
    }
}
