package net.bytebuddy;

import net.bytebuddy.utility.CompoundList;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * A cache for storing types without strongly referencing any class loader or type.
 * </p>
 * <p>
 * <b>Note</b>: In order to clean obsolete class loader references from the map, {@link TypeCache#expungeStaleEntries()} must be called
 * regularly. This can happen in a different thread, in custom intervals or on every use of the cache by creating an instance of
 * {@link WithInlineExpunction}. This cache is fully thread-safe.
 * </p>
 * <p>
 * <b>Important</b>: The behavior of a type cache might not be as expected. A class is only eligible for garbage collection once its class
 * loader is eligible for garbage collection. At the same time, a garbage collector is only eligible for garbage collection once all of
 * its classes are eligible for garbage collection. If this cache referenced the cached type strongly, this would never be the case which
 * is why this cache maintains either strong or weak references. In the latter case, a type is typically retained until the last instance of
 * the type is not eligible for garbage collection. With soft references, the type is typically retained until the next full garbage collection
 * where all instances of the type are eligible for garbage collection.
 * </p>
 *
 * @param <T> The type of the key that is used for identifying stored classes per class loader. Such keys must not strongly reference any
 *            types or class loaders without potentially corrupting the garbage eligibility of stored classes. As the storage is segmented
 *            by class loader, it is normally sufficient to store types by their name.
 * @see WithInlineExpunction
 * @see SimpleKey
 */
public class TypeCache<T> extends ReferenceQueue<ClassLoader> {

    /**
     * Indicates that a type was not found.
     */
    private static final Class<?> NOT_FOUND = null;

    /**
     * Represents the boot class loader which is {@code null} and which is therefore incompatible with system hash code computation.
     */
    private static final ClassLoader BOOT_LOADER_REPRESENTATIVE = new URLClassLoader(new URL[0]);

    /**
     * The reference type to use for stored types.
     */
    protected final Sort sort;

    /**
     * The underlying map containing cached objects.
     */
    protected final ConcurrentMap<Object, ConcurrentMap<T, Reference<Class<?>>>> cache;

    /**
     * Creates a new type cache.
     *
     * @param sort The reference type to use for stored types.
     */
    public TypeCache(Sort sort) {
        this.sort = sort;
        cache = new ConcurrentHashMap<Object, ConcurrentMap<T, Reference<Class<?>>>>();
    }

    /**
     * Finds a stored type or returns {@code null} if no type was stored.
     *
     * @param classLoader The class loader for which this type is stored.
     * @param key         The key for the type in question.
     * @return The stored type or {@code null} if no type was stored.
     */
    public Class<?> find(ClassLoader classLoader, T key) {
        ConcurrentMap<T, Reference<Class<?>>> storage = cache.get(LookupKey.of(classLoader));
        if (storage == null) {
            return NOT_FOUND;
        } else {
            Reference<Class<?>> reference = storage.get(key);
            if (reference == null) {
                return NOT_FOUND;
            } else {
                return reference.get();
            }
        }
    }

    /**
     * Inserts a new type into the cache. If a type with the same class loader and key was inserted previously, the cache is not updated.
     *
     * @param classLoader The class loader for which this type is stored.
     * @param key         The key for the type in question.
     * @param type        The type to insert of no previous type was stored in the cache.
     * @return The supplied type or a previously submitted type for the same class loader and key combination.
     */
    public Class<?> insert(ClassLoader classLoader, T key, Class<?> type) {
        ConcurrentMap<T, Reference<Class<?>>> storage = cache.get(LookupKey.of(classLoader));
        if (storage == null) {
            storage = new ConcurrentHashMap<T, Reference<Class<?>>>();
            ConcurrentMap<T, Reference<Class<?>>> previous = cache.putIfAbsent(StorageKey.of(classLoader, this), storage);
            if (previous != null) {
                storage = previous;
            }
        }
        Reference<Class<?>> reference = sort.wrap(type), previous = storage.putIfAbsent(key, reference);
        while (previous != null) {
            Class<?> previousType = previous.get();
            if (previousType != null) {
                return previousType;
            } else if (storage.remove(key, previous)) {
                previous = storage.putIfAbsent(key, reference);
            } else {
                previous = storage.get(key);
                if (previous == null) {
                    previous = storage.putIfAbsent(key, reference);
                }
            }
        }
        return type;
    }

    /**
     * Finds an existing type or inserts a new one if the previous type was not found.
     *
     * @param classLoader The class loader for which this type is stored.
     * @param key         The key for the type in question.
     * @param lazy        A lazy creator for the type to insert of no previous type was stored in the cache.
     * @return The lazily created type or a previously submitted type for the same class loader and key combination.
     */
    public Class<?> findOrInsert(ClassLoader classLoader, T key, Callable<Class<?>> lazy) {
        Class<?> type = find(classLoader, key);
        if (type != null) {
            return type;
        } else {
            try {
                return insert(classLoader, key, lazy.call());
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("Could not create type", throwable);
            }
        }
    }


    /**
     * Finds an existing type or inserts a new one if the previous type was not found.
     *
     * @param classLoader The class loader for which this type is stored.
     * @param key         The key for the type in question.
     * @param lazy        A lazy creator for the type to insert of no previous type was stored in the cache.
     * @param monitor     A monitor to lock before creating the lazy type.
     * @return The lazily created type or a previously submitted type for the same class loader and key combination.
     */
    public Class<?> findOrInsert(ClassLoader classLoader, T key, Callable<Class<?>> lazy, Object monitor) {
        Class<?> type = find(classLoader, key);
        if (type != null) {
            return type;
        } else {
            synchronized (monitor) {
                return findOrInsert(classLoader, key, lazy);
            }
        }
    }

    /**
     * Removes any stale class loader entries from the cache.
     */
    public void expungeStaleEntries() {
        Object reference;
        while ((reference = poll()) != null) {
            cache.remove(reference);
        }
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "TypeCache{" +
                "sort=" + sort +
                ", cache=" + cache +
                '}';
    }

    /**
     * Determines the storage format for a cached type.
     */
    public enum Sort {

        /**
         * Creates a cache where cached types are wrapped by {@link WeakReference}s.
         */
        WEAK {
            @Override
            protected Reference<Class<?>> wrap(Class<?> type) {
                return new WeakReference<Class<?>>(type);
            }
        },

        /**
         * Creates a cache where cached types are wrapped by {@link SoftReference}s.
         */
        SOFT {
            @Override
            protected Reference<Class<?>> wrap(Class<?> type) {
                return new SoftReference<Class<?>>(type);
            }
        };

        /**
         * Wrapes a type as a {@link Reference}.
         *
         * @param type The type to wrap.
         * @return The reference that represents the type.
         */
        protected abstract Reference<Class<?>> wrap(Class<?> type);

        @Override
        public String toString() {
            return "TypeCache.Sort." + name();
        }
    }

    /**
     * A key used for looking up a previously inserted class loader cache.
     */
    protected static class LookupKey {

        /**
         * The referenced class loader.
         */
        private final ClassLoader classLoader;

        /**
         * The class loader's identity hash code.
         */
        private final int hashCode;

        /**
         * Creates a new lookup key.
         *
         * @param classLoader A class loader representing the referenced class loader which is never {@code null}.
         */
        protected LookupKey(ClassLoader classLoader) {
            this.classLoader = classLoader;
            hashCode = System.identityHashCode(classLoader);
        }

        /**
         * Creates a lookup key.
         *
         * @param classLoader The class loader to represent.
         * @return An appropriate lookup key.
         */
        protected static Object of(ClassLoader classLoader) {
            return new LookupKey(classLoader == null ? BOOT_LOADER_REPRESENTATIVE : classLoader);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof LookupKey && ((LookupKey) other).classLoader == classLoader)
                    || (other instanceof StorageKey && ((StorageKey) other).get() == classLoader);
        }

        @Override
        public String toString() {
            return "TypeCache.LookupKey{" +
                    "classLoader=" + classLoader +
                    ", hashCode=" + hashCode +
                    '}';
        }
    }

    /**
     * A key used for storing a class loader cache reference.
     */
    protected static class StorageKey extends WeakReference<ClassLoader> {

        /**
         * The class loader's identity hash code.
         */
        private final int hashCode;

        /**
         * Creates a new storage key.
         *
         * @param classLoader    A class loader representing the referenced class loader which is never {@code null}.
         * @param referenceQueue The reference queue to notify upon a garbage collection.
         */
        protected StorageKey(ClassLoader classLoader, ReferenceQueue<? super ClassLoader> referenceQueue) {
            super(classLoader, referenceQueue);
            this.hashCode = System.identityHashCode(classLoader);
        }

        /**
         * Creates a new storage key.
         *
         * @param classLoader    A class loader representing the referenced class loader which is never {@code null}.
         * @param referenceQueue The reference queue to notify upon a garbage collection.
         * @return An appropriate storage key.
         */
        protected static Object of(ClassLoader classLoader, ReferenceQueue<? super ClassLoader> referenceQueue) {
            return new StorageKey(classLoader == null ? BOOT_LOADER_REPRESENTATIVE : classLoader, referenceQueue);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof LookupKey && ((LookupKey) other).classLoader == get())
                    || (other instanceof StorageKey && ((StorageKey) other).get() == get());
        }

        @Override
        public String toString() {
            return "TypeCache.StorageKey{" +
                    "classLoader=" + get() +
                    ", hashCode=" + hashCode +
                    '}';
        }
    }

    /**
     * An implementation of a {@link TypeCache} where obsolete references are cleared upon any call.
     *
     * @param <S> The type of the key that is used for identifying stored classes per class loader. Such keys must not strongly reference any
     *            types or class loaders without potentially corrupting the garbage eligibility of stored classes. As the storage is segmented
     *            by class loader, it is normally sufficient to store types by their name.
     * @see TypeCache
     */
    public static class WithInlineExpunction<S> extends TypeCache<S> {

        /**
         * Creats a new type cache with inlined expunction.
         *
         * @param sort The reference type to use for stored types.
         */
        public WithInlineExpunction(Sort sort) {
            super(sort);
        }

        @Override
        public Class<?> find(ClassLoader classLoader, S key) {
            try {
                return super.find(classLoader, key);
            } finally {
                expungeStaleEntries();
            }
        }

        @Override
        public Class<?> insert(ClassLoader classLoader, S key, Class<?> type) {
            try {
                return super.insert(classLoader, key, type);
            } finally {
                expungeStaleEntries();
            }
        }

        @Override
        public Class<?> findOrInsert(ClassLoader classLoader, S key, Callable<Class<?>> builder) {
            try {
                return super.findOrInsert(classLoader, key, builder);
            } finally {
                expungeStaleEntries();
            }
        }

        @Override
        public Class<?> findOrInsert(ClassLoader classLoader, S key, Callable<Class<?>> builder, Object monitor) {
            try {
                return super.findOrInsert(classLoader, key, builder, monitor);
            } finally {
                expungeStaleEntries();
            }
        }

        @Override
        public String toString() {
            return "TypeCache.WithInlineExpunction{" +
                    "sort=" + sort +
                    ", cache=" + cache +
                    '}';
        }
    }

    /**
     * A simple key based on a collection of types where no type is strongly referenced.
     */
    public static class SimpleKey implements Serializable {

        /**
         * The referenced types.
         */
        private final Set<String> types;

        /**
         * Creates a simple cache key..
         *
         * @param type           The first type to be represented by this key.
         * @param additionalType Any additional types to be represented by this key.
         */
        public SimpleKey(Class<?> type, Class<?>... additionalType) {
            this(type, Arrays.asList(additionalType));
        }

        /**
         * Creates a simple cache key..
         *
         * @param type            The first type to be represented by this key.
         * @param additionalTypes Any additional types to be represented by this key.
         */
        public SimpleKey(Class<?> type, Collection<? extends Class<?>> additionalTypes) {
            this(CompoundList.of(type, new ArrayList<Class<?>>(additionalTypes)));
        }

        /**
         * Creates a simple cache key..
         *
         * @param types Any types to be represented by this key.
         */
        public SimpleKey(Collection<? extends Class<?>> types) {
            this.types = new HashSet<String>();
            for (Class<?> type : types) {
                this.types.add(type.getName());
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            SimpleKey simpleKey = (SimpleKey) object;
            return types.equals(simpleKey.types);
        }

        @Override
        public int hashCode() {
            return types.hashCode();
        }

        @Override
        public String toString() {
            return "TypeCache.SimpleKey{" +
                    "types=" + types +
                    '}';
        }
    }
}