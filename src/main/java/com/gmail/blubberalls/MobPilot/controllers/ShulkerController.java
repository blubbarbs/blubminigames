package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class ShulkerController extends MobController<Shulker> {
    private Random random = new Random();

    public ShulkerController(Shulker mob) {
        super(mob);
        registerAbility("Shoot Bullet", ItemStack.of(Material.NETHER_STAR), this::shootShulkerBullet, 5);
    }

    protected boolean shootShulkerBullet() {
        if (entity.getPeek() < 1)
            return false;

        LivingEntity target = null;
        double lowestHeuristic = -1;

        for (LivingEntity le : entity.getLocation().getNearbyLivingEntities(30, 2)) {
            if (le == entity || le == player)
                continue;

            Vector v1 = le.getLocation().subtract(entity.getEyeLocation()).toVector();
            double angle = v1.angle(entity.getEyeLocation().getDirection());

            if (Math.toDegrees(angle) > 180)
                continue;

            double heuristic = angle * entity.getLocation().distance(le.getLocation());

            if (target == null || heuristic < lowestHeuristic) {
                lowestHeuristic = heuristic;
                target = le;
            }
        }

        if (target == null)
            return false;

        ShulkerBullet bullet = entity.getWorld().createEntity(entity.getEyeLocation(), ShulkerBullet.class);
        bullet.setShooter(target);
        bullet.setTarget(target);
        bullet.spawnAt(entity.getEyeLocation());
        entity.getWorld().playSound(entity, Sound.ENTITY_SHULKER_SHOOT, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1);
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!player.getCurrentInput().isJump() && !player.getCurrentInput().isSneak() && entity.getPeek() > 0f)
            entity.setPeek(0f);
        else if (player.getCurrentInput().isSneak() && !player.getCurrentInput().isJump() && entity.getPeek() < .3f)
            entity.setPeek(.3f);
        else if (player.getCurrentInput().isJump() && entity.getPeek() < 1f)
            entity.setPeek(1f);
    }
}
