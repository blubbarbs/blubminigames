package com.gmail.blubberalls.MobPilot;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public abstract class Controller<T extends Entity> implements Listener {
    public static PotionEffect INVISIBILITY_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY,
                                                              999999, 1, true, false);

    private int tickSchedulerID;
    private boolean isUsingItem = false;
    private double scale = 0;
    protected Player player = null;
    protected ItemStack[] playerInventory;
    protected AttributeModifier[] playerScaleModifiers;
    protected PotionEffect[] playerPotionEffects;
    protected double playerScale;
    protected T entity;

    public Controller(T entity, double scale) {
        this.entity = entity;
        this.scale = scale;
    }

    public Controller(T entity) {
        this(entity, 0.0f);
    }

    public T getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPilot(Player player) {
        if (this.player != null)
            return;

        Collection<AttributeModifier> scaleModifiers = player.getAttribute(Attribute.SCALE).getModifiers();
        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();

        this.player = player;
        this.playerInventory = player.getInventory().getContents();
        this.playerScaleModifiers = scaleModifiers.toArray(new AttributeModifier[scaleModifiers.size()]);
        this.playerPotionEffects = potionEffects.toArray(new PotionEffect[potionEffects.size()]);
        this.playerScale = player.getAttribute(Attribute.SCALE).getBaseValue();

        player.getInventory().clear();
        for (PotionEffect effect : playerPotionEffects) {
            player.removePotionEffect(effect.getType());
        }
        for (AttributeModifier modifier : playerScaleModifiers) {
            player.getAttribute(Attribute.SCALE).removeModifier(modifier);
        }

        player.getAttribute(Attribute.SCALE).setBaseValue(scale);
        player.addPotionEffect(INVISIBILITY_EFFECT);
        entity.addPassenger(player);
        Bukkit.getPluginManager().registerEvents(this, BlubMinigames.getInstance());
        tickSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(BlubMinigames.getInstance(), this::tick, 0L, 1L);
        MobPilot.registerController(player, this);
    }

    public void removePilot() {
        if (player == null)
            return;

        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(tickSchedulerID);

        player.getInventory().setContents(playerInventory);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.getAttribute(Attribute.SCALE).setBaseValue(playerScale);
        for (PotionEffect effect : playerPotionEffects) {
            player.addPotionEffect(effect);
        }
        for (AttributeModifier modifier : playerScaleModifiers) {
            player.getAttribute(Attribute.SCALE).addModifier(modifier);
        }

        if (!entity.isDead())
            entity.removePassenger(player);

        MobPilot.deregisterController(player);
        player = null;
    }

    protected void tick() {
        entity.setRotation(player.getYaw(), player.getPitch() - 25);

        if (player.hasActiveItem()) {
            if (player.getActiveItemUsedTime() == 0) {
                onStartUsingItem();
                isUsingItem = true;
            }
            else
                onUsingItem();
        }
        else if (!player.hasActiveItem() && isUsingItem) {
            onStopUsingItem();
            isUsingItem = false;
        }

        if (entity.isDead())
            removePilot();
    }

    protected void onStartUsingItem() {}

    protected void onUsingItem() {}

    protected void onStopUsingItem() {}

    protected void onMove(float forwards, float sideways) {}

    protected void onStartJump() {}

    protected void onStopJump() {}

    protected void onStartSneak() {}

    protected void onStopSneak() {}

    protected void onStartSprint() {}

    protected void onStopSprint() {}

    protected void onSwing() {}

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() == entity)
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (event.getEntity() == entity)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDismountEntity(EntityDismountEvent event) {
        if (event.getEntity() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() != entity)
            return;

        removePilot();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() != player)
            return;

        event.setKeepInventory(true);
        removePilot();
    }

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        if (event.getPlayer() != player)
            return;

        if (event.getInput().isSprint() && !player.getCurrentInput().isSprint())
            onStartSprint();
        else if (!event.getInput().isSprint() && player.getCurrentInput().isSprint())
            onStopSprint();
        else if (event.getInput().isJump() && !player.getCurrentInput().isJump())
            onStartJump();
        else if (!event.getInput().isJump() && player.getCurrentInput().isJump())
            onStopJump();
        else if (event.getInput().isSneak() && !player.getCurrentInput().isSneak())
            onStartSneak();
        else if (event.getInput().isSneak() && player.getCurrentInput().isSneak())
            onStopSneak();

        float newX = event.getInput().isRight() ? 1 : 0;
        newX -= event.getInput().isLeft() ? 1 : 0;
        float oldX = player.getSidewaysMovement();
        float newZ = event.getInput().isForward() ? 1 : 0;
        newZ -= event.getInput().isForward() ? 1 : 0;
        float oldZ = player.getForwardsMovement();

        if (newX != oldX || newZ != oldZ)
            onMove(newX, newZ);
    }

    @EventHandler
    public void onPlayerLeftSwing(PlayerInteractEvent event) {
        if (event.getPlayer() != player || !event.getAction().isLeftClick())
            return;

        onSwing();
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() != player)
            return;

        onSwing();
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamageBlock(BlockDamageEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }
}
