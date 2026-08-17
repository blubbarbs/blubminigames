package com.gmail.blubberalls.MobPilot;

import com.gmail.blubberalls.MobPilot.command.DismountCommand;
import com.gmail.blubberalls.MobPilot.controllers.*;
import com.gmail.blubberalls.minigames.BlubMinigames;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public class MobPilot implements Listener {
    private static final HashMap<UUID, MobController<?>> activeControllers = new HashMap<>();
    private static final HashMap<Class<? extends Entity>, Function<Entity, MobController<? extends Entity>>> controllerFactories = new HashMap<>();

    static {
        register(Creeper.class, CreeperController::new);
        register(Zombie.class, ZombieController::new);
        register(Husk.class, ZombieController::new);
        register(Drowned.class,  ZombieController::new);
        register(Slime.class, CubeController::new);
        register(MagmaCube.class, CubeController::new);
        register(SulfurCube.class, CubeController::new);
        register(Skeleton.class, SkeletonController::new);
        register(WitherSkeleton.class, SkeletonController::new);
        register(Stray.class, SkeletonController::new);
        register(Bogged.class, SkeletonController::new);
        register(Parched.class, SkeletonController::new);
        register(Spider.class, (spider) -> new MobController<>(spider).setScale(.15f).setCanAttack(true) );
        register(Enderman.class, EndermanController::new);
        register(IronGolem.class, IronGolemController::new);
        register(Warden.class, WardenController::new);
        register(Blaze.class, BlazeController::new);
        register(Shulker.class, ShulkerController::new);
        register(Sheep.class, SheepController::new);
        register(Breeze.class, BreezeController::new);
        register(Snowman.class, SnowGolemController::new);
        register(CopperGolem.class, CopperGolemController::new);
        register(Allay.class, AllayController::new);
        register(Wither.class, WitherController::new);
        register(Squid.class, SquidController::new);
        register(GlowSquid.class, SquidController::new);
        register(Illusioner.class, IllusionerController::new);
        register(Armadillo.class, ArmadilloController::new);
        register(Bat.class, BatController::new);
        register(Cod.class, FishController::new);
        register(TropicalFish.class, FishController::new);
        register(Salmon.class, FishController::new);
        register(Guardian.class, FishController::new);
        register(ElderGuardian.class, FishController::new);
    }

    private static <T extends Entity> void register(Class<T> clazz, Function<T, MobController<? super T>> controllerFactory) {
        controllerFactories.put(clazz, entity -> controllerFactory.apply((T) entity));
    }

    public static void bukkitRegister() {
        BlubMinigames.getInstance().registerCommand("dismount", new DismountCommand());
    }

    static void trackControllerInstance(Player player, MobController<?> controller) {
        activeControllers.put(player.getUniqueId(), controller);
    }

    static void deregisterController(Player player) {
        activeControllers.remove(player.getUniqueId());
    }

    public static MobController<?> getController(Player player) {
        return activeControllers.get(player.getUniqueId());
    }

    public static boolean hasController(Player player) {
        return activeControllers.containsKey(player.getUniqueId());
    }

    public static MobController<?> createController(Mob mob) {
        if (controllerFactories.containsKey(mob.getType().getEntityClass()))
            return controllerFactories.get(mob.getType().getEntityClass()).apply(mob);
        else
            return new MobController<>(mob);
    }
}
