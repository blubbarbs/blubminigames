package com.gmail.blubberalls.MobPilot.controllers;

import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftWarden;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class WardenController extends MobController<Warden> {
    private int sonicBoomCooldown = 0;
    private int sonicBoomLaunch = -1;

    public WardenController(Warden mob) {
        super(mob);
    }

    @Override
    public void doSwing() {
        entity.playEffect(EntityEffect.ENTITY_ATTACK);
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (sonicBoomLaunch > 0) {
            sonicBoomLaunch--;

            if (sonicBoomLaunch == 0)
                launchSonicBoom();
        }

        sonicBoomCooldown = sonicBoomCooldown >= 1 ? sonicBoomCooldown - 1 : 0;
    }

    @Override
    public void onStartSneak() {
        if (sonicBoomCooldown == 0 && sonicBoomLaunch == -1)
            startSonicBoom();
    }

    public void launchSonicBoom() {
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

        sonicBoomLaunch = -1;
        sonicBoomCooldown = 60;
    }

    public void startSonicBoom() {
        entity.playEffect(EntityEffect.WARDEN_SONIC_ATTACK);
        entity.getWorld().playSound(entity, Sound.ENTITY_WARDEN_SONIC_CHARGE,3.0f, 3.0f);
        sonicBoomLaunch = 34;
    }

}
