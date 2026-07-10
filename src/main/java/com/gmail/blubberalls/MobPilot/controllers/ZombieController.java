package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.entity.Zombie;

public class ZombieController extends MobController<Zombie> {
    public ZombieController(Zombie mob) {
        super(mob, Capability.ATTACK, Capability.ARMOR, Capability.HAND, Capability.OFFHAND);
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
}
