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
        MobPilot.bukkitRegister();
    }

    @Override
    public void onDisable() {

    }

    public static BlubMinigames getInstance() {
        return instance;
    }

    @EventHandler
    public void onPickEntity(PlayerPickEntityEvent event) {
        if (MobPilot.hasController(event.getPlayer()))
            return;

        Controller<?> controller = MobPilot.createController(event.getEntity());

        if (controller == null)
            return;

        controller.setPilot(event.getPlayer());
        event.setCancelled(true);
    }
}
