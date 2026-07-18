package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.entity.CopperGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class CopperGolemController extends MobController<CopperGolem> {
    public CopperGolemController(CopperGolem mob) {
        super(mob);
    }

    @EventHandler
    @Override
    public void onOpenInventory(InventoryOpenEvent event) {
        if (event.getPlayer() != player)
            return;

        if (event.getInventory().getType() != InventoryType.CHEST) {
            event.setCancelled(true);
            return;
        }

        entity.setGolemState(CopperGolem.State.GETTING_ITEM);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (event.getPlayer() != player)
            return;

        entity.setGolemState(CopperGolem.State.IDLE);
    }
}
