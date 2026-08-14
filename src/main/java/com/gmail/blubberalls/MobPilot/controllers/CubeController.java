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
        float speed = (float) (speedModifier * entity.getAttribute(Attribute.MOVEMENT_SPEED).getValue());

        if (player.getCurrentInput().isJump() && (entity.isOnGround() || entity.isInWater())) {
            ((CraftSlime) entity).getHandle().setSpeed(speed);
            entity.setJumping(true);
        }
        else {
            ((CraftSlime) entity).getHandle().setZza(0);
            ((CraftSlime) entity).getHandle().setSpeed(0);
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
