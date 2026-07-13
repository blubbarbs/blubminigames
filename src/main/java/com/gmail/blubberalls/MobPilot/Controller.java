package com.gmail.blubberalls.MobPilot;

import com.gmail.blubberalls.minigames.BlubMinigames;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Supplier;

public abstract class Controller<T extends Entity> implements Listener {
    public static class Capability {
        public static String ATTACK = "attack";
        public static String HAND = "hold_equipment_left";
        public static String OFFHAND = "hold_equipment_right";
        public static String ARMOR = "armor";
    }

    public static PotionEffect INVISIBILITY_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY,
                                                        PotionEffect.INFINITE_DURATION, 1, false, false, false);
    private int tickSchedulerID;
    protected ItemStack itemInUse = null;
    protected Player player = null;
    protected GameMode playerGameMode;
    protected ItemStack[] playerInventory;
    protected HashMap<Attribute, Collection<AttributeModifier>> playerAttributeModifiers = new HashMap<>();
    protected Collection<PotionEffect> playerPotionEffects;
    protected AttributeModifier scaleModifier;
    protected AttributeModifier reachModifier;
    protected Set<String> capabilities = new HashSet<>();
    protected Set<EquipmentSlot> equipmentSlots = new HashSet<>();
    protected T entity;
    protected ArrayList<ItemStack> abilities = new  ArrayList<>();
    protected HashMap<ItemStack, Supplier<Boolean>> abilityRunnables = new HashMap<>();

    public Controller(T entity, double scale, double reach, String... capabilities) {
        this.entity = entity;
        this.capabilities.addAll(Arrays.stream(capabilities).toList());
        scaleModifier = new AttributeModifier(new NamespacedKey(BlubMinigames.getInstance(), "scale_modifier"), scale - Attribute.SCALE.getDefaultValue(), AttributeModifier.Operation.ADD_SCALAR);
        reachModifier = new AttributeModifier(new NamespacedKey(BlubMinigames.getInstance(), "reach_modifier"), reach - Attribute.ENTITY_INTERACTION_RANGE.getDefaultValue(), AttributeModifier.Operation.ADD_SCALAR);

        if (this.capabilities.contains(Capability.ARMOR)) {
            equipmentSlots.add(EquipmentSlot.HEAD);
            equipmentSlots.add(EquipmentSlot.CHEST);
            equipmentSlots.add(EquipmentSlot.LEGS);
            equipmentSlots.add(EquipmentSlot.FEET);
        }

        if (this.capabilities.contains(Capability.HAND))
            equipmentSlots.add(EquipmentSlot.HAND);

        if (this.capabilities.contains(Capability.OFFHAND))
            equipmentSlots.add(EquipmentSlot.OFF_HAND);
    }

    public Controller(T entity, double scale, String... capabilities) {
        this(entity, scale, Attribute.ENTITY_INTERACTION_RANGE.getDefaultValue(), capabilities);
    }

    public Controller(T entity, String... capabilities) {
        this(entity, 0.0f, capabilities);
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

        this.player = player;

        playerInventory = player.getInventory().getContents();
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);

        playerPotionEffects = player.getActivePotionEffects();
        for (PotionEffect effect : playerPotionEffects) {
            player.removePotionEffect(effect.getType());
        }

        for (Attribute attribute : Registry.ATTRIBUTE) {
            if (player.getAttribute(attribute) == null)
                continue;

            Collection<AttributeModifier> modifiers = player.getAttribute(attribute).getModifiers();

            if (modifiers.isEmpty())
                continue;

            playerAttributeModifiers.put(attribute, modifiers);

            for (AttributeModifier modifer : modifiers) {
                player.getAttribute(attribute).removeModifier(modifer);
            }
        }

        playerGameMode = player.getGameMode();
        player.setGameMode(GameMode.CREATIVE);

        applyPlayerEffects();
        initializePlayerEquipment();

        entity.addPassenger(player);
        Bukkit.getPluginManager().registerEvents(this, BlubMinigames.getInstance());
        tickSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(BlubMinigames.getInstance(), this::tick, 0L, 1L);
        MobPilot.trackControllerInstance(player, this);

        onInitialize();
    }

    public void removePilot() {
        if (player == null)
            return;

        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(tickSchedulerID);

        player.getInventory().setContents(playerInventory);

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        for (PotionEffect potionEffect : playerPotionEffects) {
            player.addPotionEffect(potionEffect);
        }

        for (Attribute attribute : Registry.ATTRIBUTE) {
            if (player.getAttribute(attribute) == null)
                continue;

            Collection<AttributeModifier> modifiers = player.getAttribute(attribute).getModifiers();

            if (modifiers.isEmpty())
                continue;

            for (AttributeModifier modifer : modifiers) {
                player.getAttribute(attribute).removeModifier(modifer);
            }
        }

        for (Attribute attribute : playerAttributeModifiers.keySet()) {
            for (AttributeModifier attributeModifier : playerAttributeModifiers.get(attribute)) {
                player.getAttribute(attribute).addModifier(attributeModifier);
            }
        }

        player.setGameMode(playerGameMode);

        if (!entity.isDead())
            entity.removePassenger(player);

        onDeinitialize();

        MobPilot.deregisterController(player);
        player = null;
    }

    protected void registerAbility(String name, ItemStack icon, Supplier<Boolean> callback, float cooldownSeconds) {
        int index = abilities.size() + 1;
        ItemStack abilityStack = new ItemStack(Material.STICK, 1);

        abilityStack.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        abilityStack.setData(DataComponentTypes.ITEM_MODEL, icon.getType().getKey());
        abilityStack.setData(DataComponentTypes.ITEM_NAME, Component.text(name));
        abilityStack.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldownSeconds)
                .cooldownGroup(new NamespacedKey(BlubMinigames.getInstance(), "" + index)).build());

        abilities.add(abilityStack);
        abilityRunnables.put(abilityStack, callback);
    }

    protected void initializePlayerEquipment() {
        int index = 1;
        for (ItemStack stack : abilities) {
            player.getInventory().setItem(index++, stack);
        }
    }

    protected void applyPlayerEffects() {
        player.addPotionEffect(INVISIBILITY_EFFECT);
        player.getAttribute(Attribute.SCALE).addModifier(scaleModifier);
        player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).addModifier(reachModifier);
    }

    protected void tick() {
        entity.setRotation(player.getYaw(), player.getPitch() - 25);

        if (player.hasActiveItem()) {
            if (player.getActiveItemUsedTime() == 0) {
                onStartUsingItem();
                itemInUse = player.getActiveItem();
            }
            else
                onUsingItem();
        }
        else if (!player.hasActiveItem() && itemInUse != null) {
            onStopUsingItem();
            itemInUse = null;
        }

        if (entity.isDead() || entity.getPassengers().isEmpty())
            removePilot();
    }

    protected void swingAnimation() {}

    protected void onInitialize() {}

    protected void onDeinitialize() {}

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
        else if (!event.getInput().isSneak() && player.getCurrentInput().isSneak())
            onStopSneak();

        float newX = event.getInput().isRight() ? 1 : 0;
        newX -= event.getInput().isLeft() ? 1 : 0;
        float oldX = player.getSidewaysMovement();
        float newZ = event.getInput().isForward() ? 1 : 0;
        newZ -= event.getInput().isBackward() ? 1 : 0;
        float oldZ = player.getForwardsMovement();

        if (newX != oldX || newZ != oldZ)
            onMove(newZ, newX);
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

    @EventHandler
    public void onPlayerPickupItem(PlayerAttemptPickupItemEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapItems(PlayerSwapHandItemsEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRightClickAbility(PlayerInteractEvent event) {
        if (event.getPlayer() != player || !event.getAction().isRightClick() || event.getItem() == null)
            return;

        if (!abilities.contains(event.getItem()) || player.getCooldown(event.getItem()) > 0)
            return;

        Supplier<Boolean> callback = abilityRunnables.get(event.getItem());
        float cooldownSeconds = event.getItem().getData(DataComponentTypes.USE_COOLDOWN).seconds();

        if (callback.get()) {
            player.setCooldown(event.getItem(), (int) (cooldownSeconds * 20));
        }

        event.setCancelled(true);
    }
}
