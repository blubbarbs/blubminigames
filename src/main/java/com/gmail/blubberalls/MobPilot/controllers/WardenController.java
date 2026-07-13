package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.minigames.BlubMinigames;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftWarden;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.xml.crypto.Data;
import java.util.HashSet;

public class WardenController extends MobController<Warden> {
    protected int sonicBoomScheduleID = -1;

    public WardenController(Warden mob) {
        super(mob, Capability.ATTACK);
        registerAbility("Sonic Boom", ItemStack.of(Material.ECHO_SHARD), this::launchSonicBoom, 10);
    }

    @Override
    protected void onDeinitialize() {
        super.onDeinitialize();
        Bukkit.getScheduler().cancelTask(sonicBoomScheduleID);
    }

    @Override
    public void swingAnimation() {
        entity.playEffect(EntityEffect.ENTITY_ATTACK);
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }

    protected void sonicBoomProjectile() {
        CraftWarden craftWarden = (CraftWarden) entity;

        Location source = entity.getLocation().add(0, 1.5, 0);
        Location destination = player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(25));

        Vector delta = destination.toVector().subtract(source.toVector());
        Vector normalized = delta.clone().normalize();
        int steps = (int) Math.floor(delta.length());

        HashSet<LivingEntity> hit = new HashSet<>();
        for (int i = 1; i <= steps; i++) {
            Location location = source.clone().add(normalized.clone().multiply(i));

            location.getWorld().spawnParticle(Particle.SONIC_BOOM, location.getX(), location.getY(), location.getZ(), 1);
            hit.addAll(location.getNearbyLivingEntities(.5));
        }

        entity.getWorld().playSound(entity, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 3.0f);

        for (LivingEntity entity : hit) {
            if (entity == this.entity)
                continue;

            CraftLivingEntity entityCB = (CraftLivingEntity) entity;
            ServerLevel nmsWorld = (ServerLevel) entityCB.getHandle().level();

            boolean hitSuccess = entityCB.getHandle().hurtServer(nmsWorld, nmsWorld.damageSources().sonicBoom(craftWarden.getHandle()), 10.0f);

            if (hitSuccess) {
                double knockbackY = .5 * (1.0 - entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getValue());
                double knockbackXZ = 2.5 * (1.0 - entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getValue());

                entityCB.getHandle().push(normalized.getX() * knockbackXZ, normalized.getY() * knockbackY, normalized.getZ() * knockbackXZ, craftWarden.getHandle());
            }
        }
    }

    protected boolean launchSonicBoom() {
        entity.playEffect(EntityEffect.WARDEN_SONIC_ATTACK);
        entity.getWorld().playSound(entity, Sound.ENTITY_WARDEN_SONIC_CHARGE,3.0f, 3.0f);
        sonicBoomScheduleID = Bukkit.getScheduler().scheduleSyncDelayedTask(BlubMinigames.getInstance(), this::sonicBoomProjectile, 34L);
        return true;
    }



}
