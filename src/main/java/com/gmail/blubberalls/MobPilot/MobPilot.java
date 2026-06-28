package com.gmail.blubberalls.MobPilot;

import com.gmail.blubberalls.MobPilot.command.DismountCommand;
import com.gmail.blubberalls.MobPilot.controllers.*;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class MobPilot implements Listener {
    private static final HashMap<UUID, Controller<?>> controllers = new HashMap<>();

    public static void register() {
        BlubMinigames.getInstance().registerCommand("dismount", new DismountCommand());
    }

    static void registerController(Player player, Controller<?> controller) {
        controllers.put(player.getUniqueId(), controller);
    }

    static void deregisterController(Player player) {
        controllers.remove(player.getUniqueId());
    }

    public static Controller<?> getController(Player player) {
        return controllers.get(player.getUniqueId());
    }

    public static boolean hasController(Player player) {
        return controllers.containsKey(player.getUniqueId());
    }
}
