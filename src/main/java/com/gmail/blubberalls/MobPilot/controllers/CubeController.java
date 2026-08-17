package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftSlime;
import org.bukkit.entity.AbstractCubeMob;


public class CubeController extends MobController<AbstractCubeMob> {
    public CubeController(AbstractCubeMob mob) {
        super(mob);
        setReach(10.0f);
        setCanStrafe(false);
    }

    @Override
    public void doSyncRotation() {
        entity.setRotation(player.getYaw(), entity.getPitch());
        entity.setBodyYaw(player.getYaw());
    }

    @Override
    protected void doJump() {
        float speedModifier = entity.isInWater() ? 1.2f : 1.0f;

        setMobSpeedModifier(speedModifier);
        setMobForwardsStrafe(getMobSpeed());
        entity.setJumping(true);
    }

    @Override
    protected void stopJump() {
        setMobForwardsStrafe(0);
        setMobSpeedModifier(0);
        entity.setJumping(false);
    }
}
