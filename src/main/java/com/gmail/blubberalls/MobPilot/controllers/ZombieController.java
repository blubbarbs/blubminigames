package com.gmail.blubberalls.MobPilot.controllers;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ZombieController extends MobController<Zombie> {
    public ZombieController(Zombie mob) {
        super(mob);
    }

    @EventHandler
    public void onPlayerLeftSwing(PlayerInteractEvent event) {
        if (event.getPlayer() != player || !event.getAction().isLeftClick())
            return;

        entity.swingMainHand();
        entity.swingOffHand();
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() != player)
            return;

        entity.swingMainHand();
        entity.swingOffHand();
    }

    @Override
    public void onStartSprint() {
        entity.setAggressive(true);
    }

    @Override
    public void onStopSprint() {
        entity.setAggressive(false);
    }
}
