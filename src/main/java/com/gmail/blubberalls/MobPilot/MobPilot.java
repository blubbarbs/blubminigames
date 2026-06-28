package com.gmail.blubberalls.MobPilot;

import com.gmail.blubberalls.MobPilot.command.DismountCommand;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class MobPilot implements Listener {
    private static final HashMap<UUID, Controller<?>> activeControllers = new HashMap<>();

    public static void register() {
        BlubMinigames.getInstance().registerCommand("dismount", new DismountCommand());
    }

    static void registerController(Player player, Controller<?> controller) {
        activeControllers.put(player.getUniqueId(), controller);
    }

    static void deregisterController(Player player) {
        activeControllers.remove(player.getUniqueId());
    }

    public static Controller<?> getController(Player player) {
        return activeControllers.get(player.getUniqueId());
    }

    public static boolean hasController(Player player) {
        return activeControllers.containsKey(player.getUniqueId());
    }
}
