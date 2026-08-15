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
        setSyncRotation(false);
    }

    @Override
    protected void tickMove() {}

    @Override
    protected void tickJump() {
        float speedModifier = entity.isInWater() ? 1.2f : 1.0f;

        if (player.getCurrentInput().isJump() && (entity.isOnGround() || entity.isInWater())) {
            setMobSpeedModifier(speedModifier);
            setMobForwardsStrafe(getMobSpeed());
            entity.setJumping(true);
        }
        else {
            setMobForwardsStrafe(0);
            setMobSpeedModifier(0);
            entity.setJumping(false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        entity.setRotation(player.getYaw(), entity.getPitch());
        entity.setBodyYaw(player.getYaw());
    }
}
