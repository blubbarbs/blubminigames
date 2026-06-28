package com.gmail.blubberalls.minigames;

import com.gmail.blubberalls.MobPilot.Controller;
import com.gmail.blubberalls.MobPilot.MobPilot;
import com.gmail.blubberalls.MobPilot.controllers.*;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BlubMinigames extends JavaPlugin implements Listener {
    private static BlubMinigames instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        MobPilot.register();
    }

    @Override
    public void onDisable() {

    }

    public static BlubMinigames getInstance() {
        return instance;
    }

    @EventHandler
    public void onPickEntity(PlayerPickEntityEvent event) {
        Controller<?> controller;

        if (event.getEntity() instanceof Zombie zombie) {
            controller = new ZombieController(zombie);
        }
        else if (event.getEntity() instanceof Creeper creeper) {
            controller = new CreeperController(creeper);
        }
        else if (event.getEntity() instanceof AbstractSkeleton skeleton) {
            controller = new SkeletonController(skeleton);
        }
        else if (event.getEntity() instanceof Spider spider) {
            controller = new MobController<>(spider, 0.15);
        }
        else if (event.getEntity() instanceof Enderman enderman) {
            controller = new EndermanController(enderman);
        }
        else if (event.getEntity() instanceof Mob mob){
            controller = new MobController<>(mob);
        }
        else {
            return;
        }

        controller.setPilot(event.getPlayer());
        event.setCancelled(true);
    }
}
