package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;

public class SnowGolemController extends MobController<Snowman> {
    public SnowGolemController(Snowman mob) {
        super(mob);
        registerAbility("Throw Snowball", ItemStack.of(Material.SNOWBALL), this::throwSnowball, 1f);
    }

    protected boolean throwSnowball() {
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, (float) Util.randomDouble(.3333, .5));
        player.launchProjectile(Snowball.class);
        return true;
    }
}
