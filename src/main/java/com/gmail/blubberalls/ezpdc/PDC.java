package com.gmail.blubberalls.ezpdc;

import io.papermc.paper.persistence.PersistentDataViewHolder;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PDC {
    /**
     * A class to hold the {@link PersistentDataType} and {@link NamespacedKey} instances to simplify accessing
     * the PDC data. This links the data type to its key, allowing for automatic serialization/deserialization
     * by using the static methods in {@link PDC}
     * @param <T> The data type to be stored at the given key
     */
    public static class Key<T> implements Keyed {
        private final NamespacedKey key;
        private final PersistentDataType<?, T> pdType;

        public Key(NamespacedKey key, PersistentDataType<?, T> pdType) {
            this.key = key;
            this.pdType = pdType;
        }

        public Key(String namespace, String key, PersistentDataType<?, T> pdType) {
            this(new NamespacedKey(namespace, key), pdType);
        }

        public Key(Plugin plugin, String key, PersistentDataType<?, T> pdType) {
            this(new NamespacedKey(plugin, key), pdType);
        }

        public Key(String key, PersistentDataType<?, T> pdType) {
            this(new NamespacedKey("ezpdc", key), pdType);
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        public PersistentDataType<?, T> getDataType() {
            return pdType;
        }
    }

    /**
     * This interface allows for serialization/deserialization of the implementing class using
     * {@link PersistentDataContainer}. This does not use any other data primitive.
     */
    public interface PDCSerializable {
        /**
         * @param clazz The class to be serialized/deserialized
         * @param constructor This should be a constructor which takes in a single {@link PersistentDataContainer}
         * instance, which will act as the deserializer
         * @return A new {@link PersistentDataType} which will handle the serialization/deserialization of
         * the class
         * @param <T> The class implementing {@link PDCSerializable}
         */
        static <T extends PDCSerializable> PersistentDataType<PersistentDataContainer, T> createDataType(Class<T> clazz, Function<PersistentDataContainer, T> constructor) {
            return new PersistentDataType<>() {
                @Override
                public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
                    return PersistentDataContainer.class;
                }

                @Override
                public @NotNull Class<T> getComplexType() {
                    return clazz;
                }

                @Override
                public @NotNull PersistentDataContainer toPrimitive(@NotNull T t, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                    PersistentDataContainer pdc = persistentDataAdapterContext.newPersistentDataContainer();
                    t.serialize(pdc);
                    return pdc;
                }

                @Override
                public @NotNull T fromPrimitive(@NotNull PersistentDataContainer pdc, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                    return constructor.apply(pdc);
                }
            };
        }

        /**
         * @param pdc An empty {@link PersistentDataContainer} to be filled with your object's data
         */
        void serialize(PersistentDataContainer pdc);
    }

    public static boolean has(PersistentDataViewHolder pdcView, Key<?> key) {
        return pdcView.getPersistentDataContainer().has(key.getKey(), key.getDataType());
    }

    public static boolean has(PersistentDataHolder holder, Key<?> key) {
        return holder.getPersistentDataContainer().has(key.getKey(), key.getDataType());
    }

    public static boolean has(PersistentDataContainer pdc, Key<?> key) {
        return pdc.has(key.getKey(), key.getDataType());
    }

    public static <T> T getOrDefault(PersistentDataViewHolder pdcView, Key<T> key, T dflt) {
        return pdcView.getPersistentDataContainer().getOrDefault(key.getKey(), key.getDataType(), dflt);
    }

    public static <T> T getOrDefault(PersistentDataHolder holder, Key<T> key, T dflt) {
        return holder.getPersistentDataContainer().getOrDefault(key.getKey(), key.getDataType(), dflt);
    }

    public static <T> T getOrDefault(PersistentDataContainer pdc, Key<T> key, T dflt) {
        return pdc.getOrDefault(key.getKey(), key.getDataType(), dflt);
    }

    public static <T> T get(PersistentDataViewHolder pdcView, Key<T> key) {
        return pdcView.getPersistentDataContainer().get(key.getKey(), key.getDataType());
    }

    public static <T> T get(PersistentDataHolder holder, Key<T> key) {
        return holder.getPersistentDataContainer().get(key.getKey(), key.getDataType());
    }

    public static <T> T get(PersistentDataContainer pdc, Key<T> key) {
        return pdc.get(key.getKey(), key.getDataType());
    }

    public static void remove(ItemStack stack, Key<?> key) {
        stack.editPersistentDataContainer(pdc -> {
            pdc.remove(key.getKey());
        });
    }

    public static void remove(PersistentDataHolder holder, Key<?> key) {
        holder.getPersistentDataContainer().remove(key.getKey());
    }

    public static void remove(PersistentDataContainer pdc, Key<?> key) {
        pdc.remove(key.getKey());
    }

    public static <T> void set(ItemStack stack, Key<T> key, T value) {
        stack.editPersistentDataContainer(pdc -> {
            pdc.set(key.getKey(), key.getDataType(), value);
        });
    }

    public static <T> void set(PersistentDataHolder holder, Key<T> key, T value) {
        holder.getPersistentDataContainer().set(key.getKey(), key.getDataType(), value);
    }

    public static <T> void set(PersistentDataContainer pdc, Key<T> key, T value) {
        pdc.set(key.getKey(), key.getDataType(), value);
    }
}