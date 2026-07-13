package com.gmail.blubberalls.MobPilot.controllers;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class EndermanController extends MobController<Enderman> {
    static ItemStack TELEPORT_ABILITY = ItemStack.of(Material.STICK, 1);

    static {
        TELEPORT_ABILITY.setData(DataComponentTypes.ITEM_MODEL, Material.ENDER_PEARL.getKey());
        TELEPORT_ABILITY.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        TELEPORT_ABILITY.setData(DataComponentTypes.ITEM_NAME, Component.text("Teleport"));
    }


    private int teleportRange;

    public EndermanController(Enderman mob, int teleportRange) {
        super(mob, .15, Capability.ATTACK);
        this.teleportRange = teleportRange;
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
        if (entity.getCarriedBlock() != null) {
            ItemStack item = ItemStack.of(entity.getCarriedBlock().getMaterial());
            if (item.getItemMeta() instanceof BlockDataMeta meta) {
                meta.setBlockData(entity.getCarriedBlock());
                item.setItemMeta(meta);
            }
            player.getInventory().setItem(0, item);
        }

        player.getInventory().setItem(1, TELEPORT_ABILITY);
    }

    @Override
    protected void onStartSprint() {
        entity.setScreaming(true);
    }

    @Override
    protected void onStopSprint() {
        entity.setScreaming(false);
    }

    @EventHandler
    @Override
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (event.getPlayer() != player)
            return;

        entity.setCarriedBlock(null);
    }

    @EventHandler
    public void onPlayerPickBlock(PlayerPickBlockEvent event) {
        if (event.getPlayer() != player || event.getPlayer().getInventory().getItem(0) != null)
            return;

        ItemStack stack = ItemStack.of(event.getBlock().getType());

        if (stack.getItemMeta() instanceof BlockDataMeta meta) {
            meta.setBlockData(event.getBlock().getBlockData());
            stack.setItemMeta(meta);
        }

        player.getInventory().setItem(0, stack);
        if (stack.getItemMeta() != null && stack.getItemMeta() instanceof BlockDataMeta meta)
            entity.setCarriedBlock(meta.getBlockData(stack.getType()));
        else
            entity.setCarriedBlock(stack.getType().createBlockData());

        event.getPlayer().getInventory().setHeldItemSlot(0);
        event.getBlock().setType(Material.AIR);
        event.setCancelled(true);
    }

    @EventHandler
    public void onEndermanEscape(EndermanEscapeEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player || !event.getAction().isRightClick() || event.getItem() == null)
            return;

        if (event.getItem().equals(TELEPORT_ABILITY) && event.getPlayer().getCooldown(TELEPORT_ABILITY) == 0) {
            if (teleport())
                player.setCooldown(event.getItem(), 15);
        }
    }

}
