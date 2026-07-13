package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractSkeleton;

public class SkeletonController extends MobController<AbstractSkeleton> {
    public SkeletonController(AbstractSkeleton mob) {
        super(mob, Capability.ATTACK, Capability.ARMOR, Capability.HAND, Capability.OFFHAND);
    }

    @Override
    public void tick() {
        super.tick();

        // Skeleton raise hands
        entity.setAggressive(player.hasActiveItem() || player.getCurrentInput().isSprint());
    }
}
