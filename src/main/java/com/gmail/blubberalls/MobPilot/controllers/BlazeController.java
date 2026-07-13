package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftBlaze;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.SmallFireball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.Random;

public class BlazeController extends MobController<Blaze> {
    private static Method setChargedMethod;

    static {
        try {
            setChargedMethod = net.minecraft.world.entity.monster.Blaze.class.getDeclaredMethod("setCharged", boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        setChargedMethod.setAccessible(true);
    }

    private static float FLIGHT_HEIGHT_LIMIT = 5;
    private static float TICKS_BETWEEN_FIREBALLS = 6;
    private static int NUM_FIREBALLS = 3;

    private int[] shootSchedulers = new int[3];
    private double maxFlightY = 0;
    private Random random = new Random();

    public BlazeController(Blaze mob) {
        super(mob, Capability.ATTACK);
        registerAbility("Shoot Fireballs", ItemStack.of(Material.FIRE_CHARGE), this::shootAbility, 5);
    }

    @Override
    public void onInitialize() {
        maxFlightY = entity.getLocation().getY() + FLIGHT_HEIGHT_LIMIT;
    }

    @Override
    public void onDeinitialize() {
        for (int id : shootSchedulers) {
            Bukkit.getScheduler().cancelTask(id);
        }
    }

    public double getTriangleDistribution(double mean, double spread) {
        return mean + spread * (random.nextDouble() - random.nextDouble());
    }

    @Override
    public void onMoveControllerPreTick() {
        super.onMoveControllerPreTick();
        entity.setJumping(false);
    }

    protected void setBlazeOnFire(boolean fire) {
        try {
            CraftBlaze blaze = (CraftBlaze) entity;
            setChargedMethod.invoke(blaze.getHandle(), fire);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected void shoot() {
        Vector direction = entity.getEyeLocation().getDirection();
        SmallFireball fireball = entity.getWorld().createEntity(entity.getLocation(), SmallFireball.class);
        direction.setX(getTriangleDistribution(direction.getX(), .1));
        direction.setZ(getTriangleDistribution(direction.getZ(), .1));
        fireball.setVelocity(direction);
        fireball.spawnAt(entity.getEyeLocation());
        fireball.setShooter(entity);
        entity.getWorld().playEffect(entity.getLocation(), Effect.BLAZE_SHOOT, 0);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
        setBlazeOnFire(true);
    }

    protected boolean shootAbility() {
        for (int i = 0; i < NUM_FIREBALLS - 1; i++) {
            int id = Bukkit.getScheduler().scheduleSyncDelayedTask(BlubMinigames.getInstance(), this::shoot, i * (long) TICKS_BETWEEN_FIREBALLS);
            shootSchedulers[i] = id;
        }
        shootSchedulers[NUM_FIREBALLS - 1] = Bukkit.getScheduler().scheduleSyncDelayedTask(BlubMinigames.getInstance(), () -> {
            shoot();
            setBlazeOnFire(false);
        }, (long) TICKS_BETWEEN_FIREBALLS * (NUM_FIREBALLS - 1));

        return true;
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