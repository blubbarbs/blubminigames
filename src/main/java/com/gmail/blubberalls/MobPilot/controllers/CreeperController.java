package com.gmail.blubberalls.MobPilot.controllers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInputEvent;

public class CreeperController extends MobController<Creeper> {
    public CreeperController(Creeper mob) {
        super(mob);
    }


    @Override
    public void onStartSprint() {
        entity.ignite(player);
    }

    @Override
    public void onStopSprint() {
        CraftCreeper craftCreeper = (CraftCreeper) entity;

        craftCreeper.setIgnited(false);
        craftCreeper.setFuseTicks(0);
        craftCreeper.getHandle().setSwellDir(-1);
    }
}
