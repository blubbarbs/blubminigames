package com.gmail.blubberalls.MobPilot.controllers;

import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ZombieController extends MobController<Zombie> {
    public ZombieController(Zombie mob) {
        super(mob);
    }

    @Override
    public void doSwing() {
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
}
