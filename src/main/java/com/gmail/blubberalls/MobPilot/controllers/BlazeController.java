package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import java.util.Random;

public class BlazeController extends MobController<Blaze> {
    private static float FLIGHT_HEIGHT_LIMIT = 5;
    private static float TICKS_BETWEEN_FIREBALLS = 6;
    private static float NUM_FIREBALLS = 3;

    private double maxFlightY = 0;
    private Random random = new Random();
    private int attackDelay = 0;

    public BlazeController(Blaze mob) {
        super(mob, Capability.ATTACK);
    }

    @Override
    public void onInitialize() {
        maxFlightY = entity.getLocation().getY() + FLIGHT_HEIGHT_LIMIT;
    }

    public double getTriangleDistribution(double mean, double spread) {
        return mean + spread * (random.nextDouble() - random.nextDouble());
    }

    @Override
    public void onMoveControllerPreTick() {
        super.onMoveControllerPreTick();
        entity.setJumping(false);
    }

    public void doAttack() {
        Vector direction = entity.getEyeLocation().getDirection();
        direction.setX(getTriangleDistribution(direction.getX(), .3));
        direction.setZ(getTriangleDistribution(direction.getZ(), .3));
        entity.launchProjectile(SmallFireball.class, direction);
    }

    @Override
    public void onStartSprint() {
        doAttack();
    }

    @Override
    public void tick() {
        super.tick();

        if (player.getCurrentInput().isJump()) {
            if (entity.isOnGround())
                maxFlightY = entity.getLocation().getY() + FLIGHT_HEIGHT_LIMIT;

            if (entity.getLocation().getY() < maxFlightY) {
                Vector newVelocity = entity.getVelocity();
                newVelocity.setY(.15);
                entity.setVelocity(newVelocity);
            }
        }
    }
}