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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import javax.xml.crypto.Data;

public class CreeperController extends MobController<Creeper> {
    static ItemStack EXPLODE_ABILITY;

    static {
        EXPLODE_ABILITY = ItemStack.of(Material.STICK, 1);
        EXPLODE_ABILITY.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        EXPLODE_ABILITY.setData(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey());
        EXPLODE_ABILITY.setData(DataComponentTypes.ITEM_NAME, Component.text("Explode"));
        EXPLODE_ABILITY.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                                                                .consumeSeconds(1.5f)
                                                                .animation(ItemUseAnimation.NONE)
                                                                .hasConsumeParticles(false)
                                                                .sound(NamespacedKey.minecraft(""))
                                                                .build());
    }

    public CreeperController(Creeper mob) {
        super(mob);
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

    @Override
    public void initializePlayerEquipment() {
        super.initializePlayerEquipment();
        player.getInventory().setItem(1, EXPLODE_ABILITY);
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        if (event.getPlayer() != player)
            return;

        if (event.getItem().equals(EXPLODE_ABILITY)) {
            event.setCancelled(true);
            player.setCooldown(EXPLODE_ABILITY, 500);
        }
    }

    @Override
    public void onStartUsingItem() {
        if (player.getActiveItem().equals(EXPLODE_ABILITY))
            entity.ignite(player);
    }

    @Override
    public void onStopUsingItem() {
        if (EXPLODE_ABILITY.equals(itemInUse)) {
            stopCreeperSwelling();
        }
    }
}
