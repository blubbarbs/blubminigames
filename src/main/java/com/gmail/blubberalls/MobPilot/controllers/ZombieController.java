package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class ZombieController extends MobController<Zombie> {
    public ZombieController(Zombie mob) {
        super(mob, Capability.ATTACK, Capability.ARMOR, Capability.HAND, Capability.OFFHAND);
        registerAbility("Pickup", ItemStack.of(Material.GRAY_BUNDLE), this::pickupAbility, 1f);
    }

    @Override
    public void swingAnimation() {
        entity.swingOffHand();
        entity.swingMainHand();
    }

    @Override
    public void onStartSprint() {
        entity.setAggressive(true);
    }

    @Override
    public void onStopSprint() {
        entity.setAggressive(false);
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
