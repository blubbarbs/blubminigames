package com.gmail.blubberalls.MobPilot.controllers;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.gmail.blubberalls.MobPilot.Controller;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Field;
import java.util.Collection;

public class MobController<T extends Mob> extends Controller<T> {
    public enum MoveControlOperation {
        MOVE_TO,
        WAIT,
        STRAFE,
        JUMP,
    }

    protected static class MoveControlWrapper extends MoveControl<net.minecraft.world.entity.Mob> {
        private static Field strafeForwardsField;
        private static Field strafeRightField;
        private static Field operationField;

        static {
            try {
                strafeForwardsField = MoveControl.class.getDeclaredField("strafeForwards");
                strafeRightField = MoveControl.class.getDeclaredField("strafeRight");
                operationField = MoveControl.class.getDeclaredField("operation");

                strafeForwardsField.setAccessible(true);
                strafeRightField.setAccessible(true);
                operationField.setAccessible(true);
            }
            catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private MoveControl<?> wrapped;
        private MobController<?> controller;
        private net.minecraft.world.entity.Mob mob;

        public MoveControlWrapper(MobController<?> controller) {
            super(((CraftMob) controller.getEntity()).getHandle());
            this.mob = ((CraftMob) controller.getEntity()).getHandle();
            this.wrapped = mob.getMoveControl();
            this.controller = controller;
        }

        public MoveControl<?> getWrapped() {
            return wrapped;
        }

        public float getStrafeForwards() {
            try {
                return strafeForwardsField.getFloat(wrapped);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public float getStrafeRight() {
            try {
                return strafeRightField.getFloat(wrapped);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public MoveControlOperation getOperation() {
            MoveControl.Operation nmsOperation;

            try {
                nmsOperation = (Operation) operationField.get(wrapped);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return switch (nmsOperation) {
                case STRAFE -> MoveControlOperation.STRAFE;
                case JUMPING -> MoveControlOperation.JUMP;
                case MOVE_TO -> MoveControlOperation.MOVE_TO;
                default -> MoveControlOperation.WAIT;
            };
        }

        @Override
        public boolean hasWanted() {
            return wrapped.hasWanted();
        }

        @Override
        public double getSpeedModifier() {
            return wrapped.getSpeedModifier();
        }

        @Override
        public double getWantedX() {
            return wrapped.getWantedX();
        }

        @Override
        public double getWantedY() {
            return wrapped.getWantedY();
        }

        @Override
        public double getWantedZ() {
            return wrapped.getWantedZ();
        }

        @Override
        public void setWantedPosition(final double x, final double y, final double z, final double speedModifier) {
            wrapped.setWantedPosition(x, y, z, speedModifier);
        }

        @Override
        public void strafe(final float forwards, final float right) {
            wrapped.strafe(forwards, right);
        }

        @Override
        public void setWait() {
            wrapped.setWait();
        }

        @Override
        public void tick() {
            controller.onMoveControllerPreTick();
            wrapped.tick();
            controller.onMoveControllerPostTick();
        }
    }

    private static final Field moveControllerField;

    static {
        try {
            moveControllerField = net.minecraft.world.entity.Mob.class.getDeclaredField("moveControl");
            moveControllerField.setAccessible(true);
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected MoveControlWrapper nmsMoveControl = null;
    protected Collection<Goal<Mob>> goals;

    public MobController(T mob, double scale) {
        super(mob, scale);
        nmsMoveControl = new MoveControlWrapper(this);
    }

    public MobController(T mob) {
        this(mob, 0.0);
    }

    @Override
    public void setPilot(Player player) {
        if (this.player != null)
            return;

        super.setPilot(player);

        CraftMob craftMob = ((CraftMob) entity);
        goals = Bukkit.getMobGoals().getAllGoals(entity);

        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(entity.getEquipment().getItemInMainHand());
        player.getInventory().setItemInOffHand(entity.getEquipment().getItemInOffHand());
        player.getInventory().setHelmet(entity.getEquipment().getHelmet());
        player.getInventory().setChestplate(entity.getEquipment().getChestplate());
        player.getInventory().setLeggings(entity.getEquipment().getLeggings());
        player.getInventory().setBoots(entity.getEquipment().getBoots());

        Bukkit.getMobGoals().removeAllGoals(entity);
        try {
            moveControllerField.set(craftMob.getHandle(), nmsMoveControl);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removePilot() {
        super.removePilot();

        CraftMob craftMob = ((CraftMob) entity);
        for (Goal<Mob> goal : goals) {
            Bukkit.getMobGoals().addGoal(entity, 1, goal);
        }
        try {
            moveControllerField.set(craftMob.getHandle(), nmsMoveControl.getWrapped());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void onMoveControllerPreTick() {
        if (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0)
            nmsMoveControl.strafe(player.getForwardsMovement(), player.getSidewaysMovement());
        else
            nmsMoveControl.setWait();

        entity.setJumping(player.getCurrentInput().isJump());
    }

    protected void onMoveControllerPostTick() {
        CraftMob craftMob = ((CraftMob) entity);
        craftMob.getHandle().setZza(player.getForwardsMovement());
        craftMob.getHandle().setXxa(player.getSidewaysMovement());
    }

    @Override
    protected void onSwing() {
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

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() != player)
            return;

        for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
            EntityEquipmentChangedEvent.EquipmentChange change = event.getEquipmentChanges().get(slot);

            entity.getEquipment().setItem(slot, change.newItem());
        }
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
