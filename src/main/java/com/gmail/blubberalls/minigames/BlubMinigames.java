package com.gmail.blubberalls.minigames;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.MobPilot.MobPilot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        if (MobPilot.hasController(event.getPlayer()) || !(event.getRightClicked() instanceof Mob mob))
            return;

        MobController<?> controller = MobPilot.createController(mob);

        if (controller == null)
            return;

        controller.setPilot(event.getPlayer());
        event.setCancelled(true);
    }
}
