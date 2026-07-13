package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import javax.xml.crypto.Data;

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
