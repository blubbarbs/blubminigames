package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftDrowned;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZombieController extends MobController<Zombie> {
    static PotionEffect SLOW_MINING_EFFECT = new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 0, false, false, false);

    public ZombieController(Zombie mob) {
        super(mob);
        setCanAttack(true);
        registerAbility("Pickup", ItemStack.of(Material.GRAY_BUNDLE), this::pickupAbility, 1f);
    }

    @Override
    public void tick() {
        super.tick();

        if (entity instanceof Drowned drowned) {
            CraftDrowned craftDrowned = (CraftDrowned) drowned;
            if (player.getCurrentInput().isJump() && entity.isInWater() && entity.getVelocity().getY() < 0)
                craftDrowned.getHandle().setSearchingForLand(true);
            else
                craftDrowned.getHandle().setSearchingForLand(false);
        }
    }

    @Override
    protected void applyPlayerEffects() {
        super.applyPlayerEffects();
        player.addPotionEffect(SLOW_MINING_EFFECT);
    }

    @Override
    public void swingAnimation() {
        entity.swingOffHand();
        entity.swingMainHand();
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        if (event.getEntity() != entity)
            return;

        Zombie zombie = (Zombie) event.getTransformedEntity();
        ZombieController newController = new ZombieController(zombie);
        Player pilot = player;

        removePilot();
        newController.setPilot(pilot);
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
