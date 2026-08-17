package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.util.Util;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Mob;
import org.bukkit.entity.TropicalFish;
import org.bukkit.util.Vector;

public class FishController extends MobController<Mob> {
    public FishController(Mob entity) {
        super(entity);}

    @Override
    protected boolean shouldMove() {
        return !isImmobile && canStrafe && entity.isInWater() && (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0 || player.getCurrentInput().isJump() || player.getCurrentInput().isSneak());
    }

    @Override
    protected boolean shouldJump() {
        return false;
    }

    @Override
    protected void doMove() {
        float speed = (float) entity.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        Vector moveVector = new Vector(player.getSidewaysMovement(), 0, player.getForwardsMovement()).normalize().multiply(speed);

        setMobSpeed(speed);
        setMobStrafe((float) moveVector.getZ(), (float) moveVector.getX());

        if (player.getCurrentInput().isSneak() || player.getCurrentInput().isJump()) {
            float vDirection = player.getCurrentInput().isJump() ? 1 : -1;
            Vector velocity = entity.getVelocity();
            velocity.setY(vDirection * 0.25 * speed);
            entity.setVelocity(velocity);
        }
    }

    @Override
    protected void stopMove() {
        setMobSpeed(0.0f);
    }

    @Override
    protected void tick() {
        super.tick();

        if (entity.isInWater()) {
            entity.setVelocity(entity.getVelocity().add(new Vector(0, 0.005, 0)));
        }
    }
}
