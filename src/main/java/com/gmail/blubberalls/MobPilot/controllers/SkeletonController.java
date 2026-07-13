package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Material;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class SkeletonController extends MobController<AbstractSkeleton> {
    public SkeletonController(AbstractSkeleton mob) {
        super(mob, Capability.ATTACK, Capability.ARMOR, Capability.HAND, Capability.OFFHAND);
        registerAbility("Pickup", ItemStack.of(Material.WHITE_BUNDLE), this::pickupAbility, 1f);
    }

    @Override
    public void tick() {
        super.tick();

        // Skeleton raise hands
        entity.setAggressive(player.hasActiveItem() || player.getCurrentInput().isSprint());
    }

    protected boolean pickupAbility() {
        Item nearbyItem = null;
        double closestDistance = Double.MAX_VALUE;

        for (Item item : entity.getWorld().getNearbyEntitiesByType(Item.class, entity.getLocation(), 1)) {
            double distance = item.getLocation().distance(entity.getLocation());

            if (nearbyItem == null || distance < closestDistance) {
                nearbyItem = item;
                closestDistance = distance;
            }
        }

        if (nearbyItem == null)
            return false;

        entity.getEquipment().setItem(nearbyItem.getItemStack().getType().getEquipmentSlot(), nearbyItem.getItemStack());
        nearbyItem.remove();

        return true;
    }
}
