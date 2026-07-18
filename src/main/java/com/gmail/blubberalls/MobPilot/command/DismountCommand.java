package com.gmail.blubberalls.MobPilot.command;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.MobPilot.MobPilot;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class DismountCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (!(commandSourceStack.getExecutor() instanceof Player player))
            return;

        MobController<?> controller = MobPilot.getController(player);

        if (controller != null)
            controller.removePilot();
    }
}
