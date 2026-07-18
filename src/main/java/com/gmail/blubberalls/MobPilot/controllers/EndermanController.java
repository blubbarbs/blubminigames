package com.gmail.blubberalls.MobPilot.controllers;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class EndermanController extends MobController<Enderman> {
    private int teleportRange;

    public EndermanController(Enderman mob, int teleportRange) {
        super(mob, .15, Capability.ATTACK);
        this.teleportRange = teleportRange;
        registerAbility("Teleport", ItemStack.of(Material.ENDER_PEARL), this::teleport, 1);
    }

    public EndermanController(Enderman mob) {
        this(mob, 30);
    }

    protected boolean teleport() {
        RayTraceResult result = player.rayTraceBlocks(teleportRange);
        if (result == null || result.getHitBlock() == null)
            return false;

        World world = player.getWorld();
        Location teleportLocation;
        BoundingBox bbox = entity.getBoundingBox();
        bbox.shift(result.getHitPosition().subtract(bbox.getCenter()));

        if (world.hasCollisionsIn(bbox)) {
            Block block = result.getHitBlock();

            while (block.isCollidable()) {
                block = block.getRelative(BlockFace.UP);
            }

            teleportLocation = block.getLocation().setRotation(player.getEyeLocation().getRotation());
        }
        else {
            teleportLocation = result.getHitPosition().toLocation(world, player.getYaw(), player.getPitch());
        }

        entity.teleport(teleportLocation);
        world.playSound(entity, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        return true;
    }

    @Override
    protected void initializePlayerEquipment() {
        super.initializePlayerEquipment();
        if (entity.getCarriedBlock() != null) {
            ItemStack item = ItemStack.of(entity.getCarriedBlock().getMaterial());
            if (item.getItemMeta() instanceof BlockDataMeta meta) {
                meta.setBlockData(entity.getCarriedBlock());
                item.setItemMeta(meta);
            }
            player.getInventory().setItem(0, item);
        }
    }

    @Override
    protected void onStartSprint() {
        entity.setScreaming(true);
    }

    @Override
    protected void onStopSprint() {
        entity.setScreaming(false);
    }

    @Override
    protected void onPlayerEquipmentChange(EquipmentSlot slot, ItemStack newItem) {
        if (slot == EquipmentSlot.HAND) {
            if (newItem.getItemMeta() != null && newItem.getItemMeta() instanceof BlockDataMeta meta)
                entity.setCarriedBlock(meta.getBlockData(newItem.getType()));
            else if (newItem.getType().isBlock())
                entity.setCarriedBlock(newItem.getType().createBlockData());
            else
                entity.setCarriedBlock(null);
        }
    }

    @EventHandler
    @Override
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (event.getPlayer() != player)
            return;

        event.getPlayer().getInventory().setItem(0, null);
    }

    @EventHandler
    public void onPlayerPickBlock(PlayerPickBlockEvent event) {
        if (event.getPlayer() != player)
            return;

        if (event.getPlayer().getInventory().getItem(0) == null) {
            ItemStack stack = ItemStack.of(event.getBlock().getType());

            if (stack.getItemMeta() instanceof BlockDataMeta meta) {
                meta.setBlockData(event.getBlock().getBlockData());
                stack.setItemMeta(meta);
            }

            player.getInventory().setItem(0, stack);
            event.getBlock().setType(Material.AIR);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEndermanEscape(EndermanEscapeEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }
}
