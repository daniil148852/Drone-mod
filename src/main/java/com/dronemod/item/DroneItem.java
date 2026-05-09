package com.dronemod.item;

import com.dronemod.entity.DroneEntity;
import com.dronemod.registry.ModEntities;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class DroneItem extends Item {
    public DroneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos().up();
            PlayerEntity player = context.getPlayer();

            DroneEntity drone = new DroneEntity(ModEntities.DRONE, world);
            drone.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            drone.setYaw(player != null ? player.getYaw() : 0);
            world.spawnEntity(drone);

            if (player != null && !player.getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }

            if (player != null) {
                player.sendMessage(Text.literal("§aDrone deployed! Link with tablet (right-click drone)."), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.dronemod.drone_item").formatted(Formatting.GRAY));
    }
}
