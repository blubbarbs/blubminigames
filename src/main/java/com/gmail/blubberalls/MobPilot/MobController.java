package com.gmail.blubberalls.MobPilot;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.gmail.blubberalls.MobPilot.nms.InactiveBrainWrapper;
import com.gmail.blubberalls.MobPilot.nms.InactiveGoalSelectorWrapper;
import com.gmail.blubberalls.MobPilot.nms.MoveControlWrapper;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class MobController<T extends Mob> extends Controller<T> {
    private static final Field moveControllerField;
    private static final Field goalSelectorField;
    private static final Field targetSelectorField;
    private static final Field brainField;

    static {
        try {
            moveControllerField = net.minecraft.world.entity.Mob.class.getDeclaredField("moveControl");
            moveControllerField.setAccessible(true);
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

    protected MoveControlWrapper nmsMoveControl;
    protected InactiveGoalSelectorWrapper nmsGoalSelector = null;
    protected InactiveGoalSelectorWrapper nmsTargetSelector = null;
    protected InactiveBrainWrapper<?> nmsBrain = null;

    public MobController(T mob, double scale, double reach, String... capabilities) {
        super(mob, scale, reach, capabilities);
        nmsMoveControl = new MoveControlWrapper(this);
    }

    public MobController(T mob, double scale, String... capabilities) {
        this(mob, scale, Attribute.ENTITY_INTERACTION_RANGE.getDefaultValue(), capabilities);
    }

    public MobController(T mob, String... capabilities) {
        this(mob, 0.0, capabilities);
    }

    @Override
    public void setPilot(Player player) {
        if (this.player != null)
            return;

        super.setPilot(player);

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
            goalSelectorField.set(craftMob.getHandle(), nmsGoalSelector);
            targetSelectorField.set(craftMob.getHandle(), nmsTargetSelector);
            brainField.set(craftMob.getHandle(), nmsBrain);

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removePilot() {
        super.removePilot();

        CraftMob craftMob = ((CraftMob) entity);

        try {
            moveControllerField.set(craftMob.getHandle(), nmsMoveControl.getWrapped());
            goalSelectorField.set(craftMob.getHandle(), nmsGoalSelector.getWrapped());
            targetSelectorField.set(craftMob.getHandle(), nmsTargetSelector.getWrapped());
            brainField.set(craftMob.getHandle(), nmsBrain.getWrapped());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initializePlayerEquipment() {
        if (!capabilities.contains(Capability.ATTACK)) {
            ItemStack noAttack = ItemStack.of(Material.STICK);

            noAttack.setData(DataComponentTypes.ITEM_MODEL, Material.BARRIER.getKey());
            noAttack.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
            noAttack.setData(DataComponentTypes.ITEM_NAME, Component.text("No Attack"));

            player.getInventory().setItem(0, noAttack);
        }
        else if (!entity.getEquipment().getItemInMainHand().isEmpty()) {
            ItemStack hand = entity.getEquipment().getItemInMainHand();
            ItemMeta meta = hand.getItemMeta();
            meta.setUnbreakable(true);
            hand.setItemMeta(meta);
            player.getInventory().setItem(0, hand);
        }

        if (capabilities.contains(Capability.OFFHAND) && !entity.getEquipment().getItemInOffHand().isEmpty()) {
            ItemStack offHand = entity.getEquipment().getItemInOffHand();
            ItemMeta meta = offHand.getItemMeta();
            meta.setUnbreakable(true);
            offHand.setItemMeta(meta);
            player.getInventory().setItemInOffHand(offHand);
        }

        if (capabilities.contains(Capability.ARMOR)) {
            ItemStack helmet = entity.getEquipment().getHelmet();
            ItemMeta helmetMeta = helmet.getItemMeta();
            ItemStack chestplate = entity.getEquipment().getChestplate();
            ItemMeta chestplateMeta = chestplate.getItemMeta();
            ItemStack leggings = entity.getEquipment().getLeggings();
            ItemMeta leggingsMeta = leggings.getItemMeta();
            ItemStack boots = entity.getEquipment().getBoots();
            ItemMeta bootsMeta = boots.getItemMeta();

            if (!helmet.isEmpty()) {
                helmetMeta.setUnbreakable(true);
                helmet.setItemMeta(helmetMeta);
                player.getInventory().setHelmet(helmet);
            }

            if (!chestplate.isEmpty()) {
                chestplateMeta.setUnbreakable(true);
                chestplate.setItemMeta(chestplateMeta);
                player.getInventory().setChestplate(chestplate);
            }

            if (!leggings.isEmpty()) {
                leggingsMeta.setUnbreakable(true);
                leggings.setItemMeta(leggingsMeta);
                player.getInventory().setLeggings(leggings);
            }

            if (!boots.isEmpty()) {
                bootsMeta.setUnbreakable(true);
                boots.setItemMeta(bootsMeta);
                player.getInventory().setBoots(boots);
            }
        }

        super.initializePlayerEquipment();
    }

    public void onMoveControllerPreTick() {
        if (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0)
            nmsMoveControl.strafe(player.getForwardsMovement(), player.getSidewaysMovement());
        else
            nmsMoveControl.setWait();

        entity.setJumping(player.getCurrentInput().isJump());
    }

    public void onMoveControllerPostTick() {
        CraftMob craftMob = ((CraftMob) entity);
        craftMob.getHandle().setZza(player.getForwardsMovement());
        craftMob.getHandle().setXxa(player.getSidewaysMovement());
    }

    @Override
    protected void swingAnimation() {
        entity.swingMainHand();
    }

    @Override
    public void onStartUsingItem() {
        if (player.getActiveItemHand() == EquipmentSlot.OFF_HAND || player.getInventory().getHeldItemSlot() == 0)
            entity.startUsingItem(player.getActiveItemHand());
    }

    @Override
    public void onUsingItem() {
        if (!entity.getEquipment().getItem(player.getActiveItemHand()).equals(player.getActiveItem()))
            return;

        if (entity.getActiveItemUsedTime() > 0)
            entity.setActiveItemRemainingTime(player.getActiveItemRemainingTime());
    }

    @Override
    public void onStopUsingItem() {
        if (entity.getActiveItemUsedTime() > 0)
            entity.clearActiveItem();
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
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() != entity)
            return;

        for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
            EntityEquipmentChangedEvent.EquipmentChange change = event.getEquipmentChanges().get(slot);

            if (slot == EquipmentSlot.HAND)
                player.getInventory().setItem(0, change.newItem());
            else if (player.canUseEquipmentSlot(slot))
                player.getInventory().setItem(slot, change.newItem());
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
}
