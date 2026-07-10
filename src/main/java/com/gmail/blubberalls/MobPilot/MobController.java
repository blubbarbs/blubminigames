package com.gmail.blubberalls.MobPilot;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.gmail.blubberalls.MobPilot.nms.InactiveBrainWrapper;
import com.gmail.blubberalls.MobPilot.nms.InactiveGoalSelectorWrapper;
import com.gmail.blubberalls.MobPilot.nms.MoveControlWrapper;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;

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

        // TODO
        // Try to add attack cool down via attributes

        super.setPilot(player);

        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(entity.getEquipment().getItemInMainHand());
        player.getInventory().setItemInOffHand(entity.getEquipment().getItemInOffHand());
        player.getInventory().setHelmet(entity.getEquipment().getHelmet());
        player.getInventory().setChestplate(entity.getEquipment().getChestplate());
        player.getInventory().setLeggings(entity.getEquipment().getLeggings());
        player.getInventory().setBoots(entity.getEquipment().getBoots());

        CraftMob craftMob = ((CraftMob) entity);
        craftMob.clearActiveItem();

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
        if (!entity.getEquipment().getItem(player.getActiveItemHand()).equals(player.getActiveItem()))
            return;

        entity.startUsingItem(player.getActiveItemHand());
    }

    @Override
    public void onUsingItem() {
        if (!entity.getEquipment().getItem(player.getActiveItemHand()).equals(player.getActiveItem()))
            return;

        entity.setActiveItemRemainingTime(player.getActiveItemRemainingTime());
    }

    @Override
    public void onStopUsingItem() {
        entity.clearActiveItem();
    }

    @Override
    public void onLeftClickEntity(Entity entity) {
        swingAnimation();

        if (capabilities.contains(Capability.ATTACK) && this.entity.getAttribute(Attribute.ATTACK_DAMAGE) != null)
            this.entity.attack(entity);
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() == player) {
            for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
                EntityEquipmentChangedEvent.EquipmentChange change = event.getEquipmentChanges().get(slot);

                if (!entity.canUseEquipmentSlot(slot) || !equipmentSlots.contains(slot))
                    continue;

                if (change.newItem().equals(entity.getEquipment().getItem(slot)))
                    continue;

                entity.getEquipment().setItem(slot, change.newItem());
            }
        }
        else if (event.getEntity() == entity) {
            for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
                EntityEquipmentChangedEvent.EquipmentChange change = event.getEquipmentChanges().get(slot);

                if (!player.canUseEquipmentSlot(slot) || !equipmentSlots.contains(slot))
                    continue;

                if (change.newItem().equals(player.getInventory().getItem(slot)))
                    continue;

                player.getInventory().setItem(slot, change.newItem());
                player.getInventory().addItem(change.oldItem());
            }
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
