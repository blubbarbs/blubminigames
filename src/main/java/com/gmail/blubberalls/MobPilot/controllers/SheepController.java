package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

public class SheepController extends MobController<Sheep> {
    private int grazingScheduleID = -1;

    public SheepController(Sheep mob) {
        super(mob);
        registerAbility("Graze", ItemStack.of(Material.SHORT_GRASS), this::graze, 10f);
    }

    @Override
    protected void onDeinitialize() {
        Bukkit.getScheduler().cancelTask(grazingScheduleID);
    }

    protected void onFinishGraze() {
        Block location = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);

        if (entity.isOnGround() && location.getType() == Material.GRASS_BLOCK && entity.getWorld().getGameRuleValue(GameRules.MOB_GRIEFING)) {
            EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity, location, Material.DIRT.createBlockData());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                // Find some other way to play the block break effect without dropping the block
                location.breakNaturally(true);
                location.setType(Material.DIRT);
            }

            entity.setSheared(false);
        }

        setImmobile(false);
    }

    protected boolean graze() {
        if (!entity.isSheared())
            return false;

        if (entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.GRASS_BLOCK)
            return false;

        setImmobile(true);
        entity.playEffect(EntityEffect.SHEEP_EAT_GRASS);
        grazingScheduleID = Bukkit.getScheduler().scheduleSyncDelayedTask(BlubMinigames.getInstance(), this::onFinishGraze, 40L);
        return true;
    }
}
