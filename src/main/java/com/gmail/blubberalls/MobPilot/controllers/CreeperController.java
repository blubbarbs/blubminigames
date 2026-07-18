package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creeper;
import org.bukkit.inventory.ItemStack;

public class CreeperController extends MobController<Creeper> {
    public CreeperController(Creeper mob) {
        super(mob);
        registerAbility("Explode", ItemStack.of(Material.GUNPOWDER), this::onExplodeAbility, 1.5f);
    }

    protected void stopCreeperSwelling() {
        CraftCreeper craftCreeper = (CraftCreeper) entity;

        craftCreeper.setIgnited(false);
        craftCreeper.setFuseTicks(0);
        craftCreeper.getHandle().setSwellDir(-1);
    }

    @Override
    public void onInitialize() {
        stopCreeperSwelling();
    }

    protected boolean onExplodeAbility() {
        entity.ignite(player);
        player.getInventory().setItem(1, null);
        return true;
    }
}
