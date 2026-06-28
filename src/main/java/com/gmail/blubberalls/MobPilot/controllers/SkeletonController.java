package com.gmail.blubberalls.MobPilot.controllers;

import org.bukkit.entity.AbstractSkeleton;

public class SkeletonController extends MobController<AbstractSkeleton> {
    public SkeletonController(AbstractSkeleton mob) {
        super(mob);
    }

    @Override
    public void tick() {
        super.tick();

        entity.setAggressive(player.hasActiveItem() || player.getCurrentInput().isSprint());
    }
}
