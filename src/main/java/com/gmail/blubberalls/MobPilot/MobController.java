package com.gmail.blubberalls.MobPilot;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.gmail.blubberalls.MobPilot.nms.*;
import com.gmail.blubberalls.ezpdc.PDC;
import com.gmail.blubberalls.minigames.BlubMinigames;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MobController<T extends Mob> implements Listener {
    private static final Field moveControllerField;
    private static final Field lookControllerField;
    private static final Field jumpControllerField;
    private static final Field goalSelectorField;
    private static final Field targetSelectorField;
    private static final Field brainField;

    static {
        try {
            moveControllerField = net.minecraft.world.entity.Mob.class.getDeclaredField("moveControl");
            moveControllerField.setAccessible(true);
            lookControllerField = net.minecraft.world.entity.Mob.class.getDeclaredField("lookControl");
            lookControllerField.setAccessible(true);
            jumpControllerField = net.minecraft.world.entity.Mob.class.getDeclaredField("jumpControl");
            jumpControllerField.setAccessible(true);
            goalSelectorField = net.minecraft.world.entity.Mob.class.getDeclaredField("goalSelector");
            goalSelectorField.setAccessible(true);
            targetSelectorField = net.minecraft.world.entity.Mob.class.getDeclaredField("targetSelector");
            targetSelectorField.setAccessible(true);
            brainField = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("brain");
            brainField.setAccessible(true);
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static class Capability {
        public static String ATTACK = "attack";
    }

    public static PotionEffect INVISIBILITY_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY,
                                                        PotionEffect.INFINITE_DURATION, 1, false, false, false);
    public static ItemStack HELD_ITEM = ItemStack.of(Material.BREEZE_ROD);
    public static PDC.Key<String> ABILITY_STRING_KEY = new PDC.Key<>(new NamespacedKey(BlubMinigames.getInstance(), "ability_key"), PersistentDataType.STRING);

    static {
        HELD_ITEM.setData(DataComponentTypes.ITEM_MODEL, Material.STRUCTURE_VOID.getKey());
        HELD_ITEM.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        HELD_ITEM.setData(DataComponentTypes.ITEM_NAME, Component.text("Held Item"));
    }

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
    protected T entity;
    protected ArrayList<ItemStack> abilityStacks = new ArrayList<>();
    protected HashMap<UUID, Function<ItemStack, Boolean>> abilityRunnables = new HashMap<>();
    protected boolean isImmobile = false;
    protected boolean canStrafe = true;
    protected boolean canJump = true;

    protected MoveControlWrapper nmsMoveControl;
    protected LookControlWrapper nmsLookControl;
    protected JumpControlWrapper nmsJumpControl;
    protected InactiveGoalSelectorWrapper nmsGoalSelector = null;
    protected InactiveGoalSelectorWrapper nmsTargetSelector = null;
    protected InactiveBrainWrapper<?> nmsBrain = null;

    public MobController(T entity, double scale, double reach, String... capabilities) {
        this.entity = entity;
        this.capabilities.addAll(Arrays.stream(capabilities).toList());
        scaleModifier = new AttributeModifier(new NamespacedKey(BlubMinigames.getInstance(), "scale_modifier"), scale - Attribute.SCALE.getDefaultValue(), AttributeModifier.Operation.ADD_SCALAR);
        reachModifier = new AttributeModifier(new NamespacedKey(BlubMinigames.getInstance(), "reach_modifier"), reach - Attribute.ENTITY_INTERACTION_RANGE.getDefaultValue(), AttributeModifier.Operation.ADD_SCALAR);
        nmsMoveControl = new MoveControlWrapper(this);
        nmsLookControl = new LookControlWrapper(this);
        nmsJumpControl = new JumpControlWrapper(this);
    }

    public MobController(T entity, double scale, String... capabilities) {
        this(entity, scale, Attribute.ENTITY_INTERACTION_RANGE.getDefaultValue(), capabilities);
    }

    public MobController(T entity, String... capabilities) {
        this(entity, 0.0f, capabilities);
    }

    public T getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }

    public void setImmobile(boolean immobile) {
        this.isImmobile = immobile;
    }

    public void setCanStrafe(boolean canStrafe) {
        this.canStrafe = canStrafe;
    }

    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    public void setPilot(Player player) {
        if (this.player != null)
            return;

        CraftMob craftMob = ((CraftMob) entity);
        craftMob.clearActiveItem();
        craftMob.setTarget(null);

        try {
            GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(craftMob.getHandle());
            GoalSelector targetSelector = (GoalSelector) targetSelectorField.get(craftMob.getHandle());

            nmsTargetSelector = new InactiveGoalSelectorWrapper(this, targetSelector);
            nmsGoalSelector = new InactiveGoalSelectorWrapper(this, goalSelector);
            nmsBrain = new InactiveBrainWrapper<>(craftMob.getHandle().getBrain());

            moveControllerField.set(craftMob.getHandle(), nmsMoveControl);
            lookControllerField.set(craftMob.getHandle(), nmsLookControl);
            jumpControllerField.set(craftMob.getHandle(), nmsJumpControl);
            goalSelectorField.set(craftMob.getHandle(), nmsGoalSelector);
            targetSelectorField.set(craftMob.getHandle(), nmsTargetSelector);
            brainField.set(craftMob.getHandle(), nmsBrain);

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

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
        player.setGameMode(GameMode.SURVIVAL);

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

        CraftMob craftMob = ((CraftMob) entity);
        try {
            moveControllerField.set(craftMob.getHandle(), nmsMoveControl.getWrapped());
            lookControllerField.set(craftMob.getHandle(), nmsLookControl.getWrapped());
            jumpControllerField.set(craftMob.getHandle(), nmsJumpControl.getWrapped());
            goalSelectorField.set(craftMob.getHandle(), nmsGoalSelector.getWrapped());
            targetSelectorField.set(craftMob.getHandle(), nmsTargetSelector.getWrapped());
            brainField.set(craftMob.getHandle(), nmsBrain.getWrapped());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerAbility(String name, ItemStack icon, Function<ItemStack, Boolean> callback, float cooldownSeconds, boolean useRawItemStack) {
        ItemStack abilityStack = useRawItemStack ? icon : new ItemStack(Material.STICK, 1);

        if (!useRawItemStack) {
            abilityStack.setData(DataComponentTypes.ITEM_MODEL, icon.getType().getKey());
        }

        UUID abilityUUID = UUID.randomUUID();
        PDC.set(abilityStack, ABILITY_STRING_KEY, abilityUUID.toString());
        abilityStack.setData(DataComponentTypes.ITEM_NAME, Component.text(name));
        abilityStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text(name));
        abilityStack.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldownSeconds)
                .cooldownGroup(new NamespacedKey(BlubMinigames.getInstance(), abilityUUID.toString())).build());

        abilityRunnables.put(abilityUUID, callback);
        abilityStacks.add(abilityStack);
    }

    protected void registerAbility(String name, ItemStack icon, Function<ItemStack, Boolean> callback, float cooldownSeconds) {
        registerAbility(name, icon, callback, cooldownSeconds, false);
    }

    protected void registerAbility(String name, ItemStack icon, Supplier<Boolean> callback, float cooldownSeconds, boolean useRawItemStack) {
        registerAbility(name, icon, (itemStack -> callback.get()), cooldownSeconds, useRawItemStack);
    }

    protected void registerAbility(String name, ItemStack icon, Supplier<Boolean> callback, float cooldownSeconds) {
        registerAbility(name, icon, callback, cooldownSeconds, false);
    }


    protected void initializePlayerEquipment() {
        player.getInventory().setItem(0, entity.getEquipment().getItemInMainHand());
        player.getInventory().setItem(EquipmentSlot.OFF_HAND, entity.getEquipment().getItemInOffHand());
        player.getInventory().setItem(EquipmentSlot.HEAD, entity.getEquipment().getHelmet());
        player.getInventory().setItem(EquipmentSlot.BODY, entity.getEquipment().getChestplate());
        player.getInventory().setItem(EquipmentSlot.LEGS, entity.getEquipment().getLeggings());
        player.getInventory().setItem(EquipmentSlot.FEET, entity.getEquipment().getBoots());

        int index = 1;
        for (ItemStack stack : abilityStacks) {
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

    public void onMoveControllerPreTick() {
        boolean isMoving = !isImmobile && canStrafe && (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0);

        if (isMoving)
            nmsMoveControl.strafe(player.getForwardsMovement(), player.getSidewaysMovement());
        else
            nmsMoveControl.setWait();

        if (!isImmobile && canJump)
            entity.setJumping(player.getCurrentInput().isJump());
    }

    public void onMoveControllerPostTick() {
        CraftMob craftMob = ((CraftMob) entity);

        if (!isImmobile && canStrafe) {
            craftMob.getHandle().setZza(player.getForwardsMovement());
            craftMob.getHandle().setXxa(player.getSidewaysMovement());
        }
        else {
            craftMob.getHandle().setZza(0);
            craftMob.getHandle().setXxa(0);
        }
    }

    protected void onPlayerEquipmentChange(EquipmentSlot slot, ItemStack newItem) {
        if (entity.canUseEquipmentSlot(slot) && !entity.getEquipment().getItem(slot).equals(newItem))
            entity.getEquipment().setItem(slot, newItem);
    }

    protected void onEntityEquipmentChange(EquipmentSlot slot, ItemStack newItem) {
        if (player.canUseEquipmentSlot(slot) && !player.getEquipment().getItem(slot).equals(newItem))
            player.getEquipment().setItem(slot, newItem);
    }

    protected void swingAnimation() {
        entity.swingMainHand();
    }

    protected void onInitialize() {}

    protected void onDeinitialize() {}

    protected void onStartUsingItem() {
        entity.startUsingItem(player.getActiveItemHand());
    }

    protected void onUsingItem() {
        entity.setActiveItemRemainingTime(player.getActiveItemRemainingTime());
    }

    protected void onStopUsingItem() {
        entity.clearActiveItem();
    }

    protected void onMove(float forwards, float sideways) {}

    protected void onStartJump() {}

    protected void onStopJump() {}

    protected void onStartSneak() {
        if (entity.getAmbientSound() != null)
            entity.getWorld().playSound(entity, entity.getAmbientSound(), SoundCategory.NEUTRAL, 1f, 1f);
    }

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
    public void onPlayerPickBlockEvent(PlayerPickBlockEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHungerChange(FoodLevelChangeEvent event) {
        if (event.getEntity() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHeldItemChange(PlayerItemHeldEvent event) {
        if (event.getPlayer() != player)
            return;

        ItemStack stack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        boolean isAbility = stack != null && PDC.has(stack, ABILITY_STRING_KEY);

        if (event.getPreviousSlot() == 0 && isAbility && player.getCooldown(stack) == 0) {
            UUID abilityUUID = UUID.fromString(PDC.get(stack, ABILITY_STRING_KEY));
            Function<ItemStack, Boolean> callback = abilityRunnables.get(abilityUUID);
            float cooldownSeconds = stack.getData(DataComponentTypes.USE_COOLDOWN).seconds();

            if (callback.apply(stack))
                player.setCooldown(stack, (int) (cooldownSeconds * 20));
        }

        if (player.hasActiveItem()) {
            player.clearActiveItem();
            entity.clearActiveItem();
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
            EntityEquipmentChangedEvent.EquipmentChange change = event.getEquipmentChanges().get(slot);

            if (event.getEntity() == player)
                onPlayerEquipmentChange(slot, change.newItem());
            else if (event.getEntity() == entity)
                onEntityEquipmentChange(slot, change.newItem());
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (event.getEntity() != entity)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() != player)
            return;

        event.setCancelled(true);

        if (capabilities.contains(Capability.ATTACK) && this.entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            swingAnimation();
            this.entity.attack(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerShootArrowEvent(EntityShootBowEvent event) {
        if (event.getEntity() != player)
            return;

        Arrow arrow = (Arrow) event.getProjectile();
        Location newLocation = entity.getEyeLocation();
        newLocation.setRotation(entity.getEyeLocation().getRotation());

        arrow.setShooter(event.getEntity());
        arrow.setCritical(false);
        arrow.setShooter(entity);
        arrow.teleport(newLocation);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
    }

    @EventHandler
    public void onPlayerShootProjectile(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile().getShooter() != player)
            return;

        Location newLocation = entity.getEyeLocation();
        newLocation.setRotation(entity.getEyeLocation().getRotation());

        event.getProjectile().setShooter(entity);
        event.getProjectile().teleport(newLocation);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onRightClickBow(PlayerInteractEvent event) {
        if (event.getPlayer() != player || !event.getAction().isRightClick() || !event.hasItem() || player.hasActiveItem())
            return;

        ItemStack stack = event.getItem();

        if (player.getInventory().contains(Material.ARROW)
                || player.getInventory().contains(Material.SPECTRAL_ARROW)
                || player.getInventory().contains(Material.TIPPED_ARROW))
            return;

        if (stack.getType() == Material.BOW || stack.getType() == Material.CROSSBOW) {
            player.getInventory().addItem(new ItemStack(Material.ARROW));
        }
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        if (event.getPlayer() != player)
            return;

        event.setCancelled(true);
    }
}
