package com.gmail.blubberalls.MobPilot.controllers;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;

public class EndermanController extends MobController<Enderman> {
    private int teleportRange;

    public EndermanController(Enderman mob, int teleportRange) {
        super(mob, .15, Capability.ATTACK);
        this.teleportRange = teleportRange;
    }

    public EndermanController(Enderman mob) {
        this(mob, 15);
    }

    @Override
    public void setPilot(Player player) {
        super.setPilot(player);

        if (entity.getCarriedBlock() != null) {
            ItemStack item = ItemStack.of(entity.getCarriedBlock().getMaterial());

            if (item.getItemMeta() instanceof BlockDataMeta meta) {
                meta.setBlockData(entity.getCarriedBlock());
                item.setItemMeta(meta);
            }

            player.getInventory().setItemInMainHand(item);
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
    protected void onStartSneak() {
        RayTraceResult result = player.rayTraceBlocks(teleportRange);
        if (result == null || result.getHitBlock() == null)
            return;

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
    }

    @EventHandler
    @Override
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {}

    @EventHandler
    public void onPlayerPickBlock(PlayerPickBlockEvent event) {
        if (event.getPlayer() != player)
            return;

        ItemStack stack = ItemStack.of(event.getBlock().getType());

        if (stack.getItemMeta() instanceof BlockDataMeta meta) {
            meta.setBlockData(event.getBlock().getBlockData());
            stack.setItemMeta(meta);
        }

        HashMap<Integer, ItemStack> result = player.getInventory().addItem(stack);

        if (result.isEmpty())
            event.getBlock().setType(Material.AIR);

        event.setCancelled(true);
    }

    @EventHandler
    @Override
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() != player)
            return;

        for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
            if (slot != EquipmentSlot.HAND)
                continue;

            ItemStack newStack = event.getEquipmentChanges().get(slot).newItem();

            if (!newStack.getType().isBlock())
                entity.setCarriedBlock(null);
            else if (newStack.getItemMeta() != null && newStack.getItemMeta() instanceof BlockDataMeta meta)
                entity.setCarriedBlock(meta.getBlockData(newStack.getType()));
            else
                entity.setCarriedBlock(newStack.getType().createBlockData());
        }
    }

    @EventHandler
    public void onEndermanEscape(EndermanEscapeEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }

}
