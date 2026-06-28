package com.gmail.blubberalls.MobPilot.controllers;

import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creeper;

public class CreeperController extends MobController<Creeper> {
    public CreeperController(Creeper mob) {
        super(mob);
    }


    @Override
    public void onStartSneak() {
        entity.ignite(player);
    }

    @Override
    public void onStopSneak() {
        CraftCreeper craftCreeper = (CraftCreeper) entity;

        craftCreeper.setIgnited(false);
        craftCreeper.setFuseTicks(0);
        craftCreeper.getHandle().setSwellDir(-1);
    }
}
