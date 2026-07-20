package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.util.Util;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftWither;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class WitherController extends FlyingMobController<Wither> {
    static int NUM_SKULLS_UNTIL_BLUE = 3;
    static ItemStack WITHER_SKULL_HEAD;
    static ItemStack WITHER_SKULL_BLUE_HEAD;

    static {
        WITHER_SKULL_HEAD = Util.createSkull("https://textures.minecraft.net/texture/1e4d204ebc242eca2148f5853e3af00f84f0d674099dc394f6d2924b240ca2e3");
        WITHER_SKULL_BLUE_HEAD = Util.createSkull("http://textures.minecraft.net/texture/5169c90c8874ab575b201b616a69eac7e0b5ac69bbcccbb2772e36776fe69441");
    }

    public WitherController(Wither entity) {
        super(entity);
        registerAbility("Shoot Main Head", WITHER_SKULL_HEAD.clone(), this::shootMainSkull, 1f, true);
        registerAbility("Shoot Left Head", WITHER_SKULL_HEAD.clone(), (itemStack -> shootSideSkull(1, itemStack)), 3f, true);
        registerAbility("Shoot Right Head", WITHER_SKULL_HEAD.clone(), (itemStack -> shootSideSkull(2, itemStack)), 3f, true);
    }

    @Override
    protected void onInitialize() {
        entity.setInvulnerableTicks(0);
    }

    public Location getHeadLocation(int head) {
        if (head == 0)
            return entity.getEyeLocation();

        double scale = entity.getAttribute(Attribute.SCALE) != null ? entity.getAttribute(Attribute.SCALE).getValue() : 1;
        double headAngle = Math.toRadians(entity.getBodyYaw() + 180 * (head - 1));
        double x = entity.getX();
        x += Math.cos(headAngle) * 1.3 * scale;
        double y = entity.getY();
        y += head <= 0 ? 3f * scale : 2.2f * scale;
        double z = entity.getZ();
        z += Math.sin(headAngle) * 1.3 * scale;

        return new Location(entity.getWorld(), x, y, z);
    }

    protected void shootSkull(int head, Location targetLocation, boolean blueSkull) {
        Location headLocation = getHeadLocation(head);
        Vector direction = targetLocation.toVector().subtract(headLocation.toVector()).normalize();
        WitherSkull skull = entity.getWorld().spawn(headLocation, WitherSkull.class);

        skull.setShooter(entity);
        skull.setVelocity(direction);
        if (blueSkull)
            skull.setCharged(true);
        entity.getWorld().playSound(entity, Sound.ENTITY_WITHER_SHOOT, 1, 1);
    }

    protected void shootSkull(int head, boolean blueSkull) {
        RayTraceResult result = entity.rayTraceBlocks(50);
        Location targetLocation;

        if (result == null) {
            targetLocation = entity.getEyeLocation().add(entity.getEyeLocation().getDirection().normalize().multiply(50));
        }
        else
            targetLocation = result.getHitPosition().toLocation(entity.getWorld());

        shootSkull(head, targetLocation, blueSkull);
    }

    protected boolean shootMainSkull(ItemStack icon) {
        shootSkull(0, false);
        return true;
    }

    protected boolean shootSideSkull(int head, ItemStack stack) {
        boolean blueSkull = false;

        if (stack.getAmount() < NUM_SKULLS_UNTIL_BLUE - 1) {
            stack.setAmount(stack.getAmount() + 1);
        }
        else if (stack.getAmount() == NUM_SKULLS_UNTIL_BLUE - 1) {
            stack.setAmount(stack.getAmount() + 1);
            stack.setData(DataComponentTypes.PROFILE, WITHER_SKULL_BLUE_HEAD.getData(DataComponentTypes.PROFILE));
        }
        else {
            blueSkull = true;
            stack.setAmount(1);
            stack.setData(DataComponentTypes.PROFILE, WITHER_SKULL_HEAD.getData(DataComponentTypes.PROFILE));
        }

        shootSkull(head, blueSkull);
        return true;
    }

    @Override
    public void onMoveControllerPreTick() {
        Vector move = Util.getRelativeMoveVector(player);

        if (player.getCurrentInput().isJump())
            move.add(new Vector(0, 1, 0));

        if (!move.isZero()) {
            float speed = ((CraftWither) entity).getHandle().getSpeed();

            move.normalize().multiply(speed);
            entity.setVelocity(entity.getVelocity().setX(move.getX()).setZ(move.getZ()).setY(move.getY() * .6f));
            nmsMoveControl.setWantedPosition(entity.getX() + move.getX(), entity.getY() + move.getY(), entity.getZ() + move.getZ(), 1.0);
        }
        else
            nmsMoveControl.setWait();
    }

    @Override
    public void onMoveControllerPostTick() {

    }
}
