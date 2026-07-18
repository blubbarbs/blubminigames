package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.minigames.BlubMinigames;
import com.gmail.blubberalls.util.Util;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Pose;
import org.bukkit.entity.WindCharge;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BreezeController extends MobController<Breeze> {
    private int jumpChargeTicks = 0;
    private boolean isShooting = false;
    private int shootingScheduler = -1;

    public BreezeController(Breeze entity) {
        super(entity);
        setCanJump(false);
        registerAbility("Wind Charge", ItemStack.of(Material.WIND_CHARGE), this::shoot, 3f);
    }

    @Override
    protected void onDeinitialize() {
        super.onDeinitialize();
        Bukkit.getScheduler().cancelTask(shootingScheduler);
    }

    protected void doLongJump() {
        double velScalar = ((double) jumpChargeTicks / 10) * 1;
        double velAdd = 0;
        Vector launchVector = Util.getRelativeMoveVector(player);

        if (entity.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            velAdd = 0.1F * (entity.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier() + 1.0F);
        }

        launchVector.multiply(velScalar);
        launchVector.setY(velScalar + velAdd);

        entity.setVelocity(launchVector);
        entity.getWorld().playSound(entity, Sound.ENTITY_BREEZE_JUMP, 1, 1);
        entity.setFrictionState(TriState.FALSE);
        entity.setPose(Pose.LONG_JUMPING);
        setImmobile(false);
        jumpChargeTicks = 0;

    }

    protected boolean shoot() {
        isShooting = true;
        entity.setPose(Pose.SHOOTING);
        setImmobile(true);
        shootingScheduler = Bukkit.getScheduler().scheduleSyncDelayedTask(BlubMinigames.getInstance(), () -> {
            isShooting = false;
            entity.setPose(Pose.STANDING);
            entity.launchProjectile(WindCharge.class);
            setImmobile(false);
            entity.getWorld().playSound(entity, Sound.ENTITY_BREEZE_SHOOT, 1.5f, 1f);
        }, 15L);

        return true;
    }

    @Override
    public void tick() {
        super.tick();
        entity.setRotation(entity.getYaw(), 0);

        if (isShooting)
            return;

        if (jumpChargeTicks == 0) {
            if (player.getCurrentInput().isJump() && entity.isOnGround()) {
                entity.setPose(Pose.INHALING);
                jumpChargeTicks++;
                setImmobile(true);
            }
            else if (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0) {
                entity.setPose(Pose.SLIDING);
            }
            else {
                entity.setPose(Pose.STANDING);
            }
        }
        else if (jumpChargeTicks > 0) {
            jumpChargeTicks = jumpChargeTicks < 10 ? jumpChargeTicks + 1 :  jumpChargeTicks;

            if (!player.getCurrentInput().isJump()) {
                doLongJump();
            }
        }

        if (entity.getFrictionState() == TriState.FALSE && (entity.isOnGround() || entity.isInWater()))
            entity.setFrictionState(TriState.TRUE);
    }

}
