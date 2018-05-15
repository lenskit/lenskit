/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.entities;

import com.google.common.base.*;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.lenskit.util.keys.KeyExtractor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public final class Entities {
    private static ConcurrentHashMap<Class<?>, Optional<Constructor<? extends EntityBuilder>>> BUILDER_CTOR_CACHE
            = new ConcurrentHashMap<>();

    private Entities() {}

    /**
     * Create a new bare entity.
     * @param type The bare entity.
     * @param id The entity ID.
     * @return An entity.
     */
    public static Entity create(EntityType type, long id) {
        return new BareEntity(type, id);
    }

    /**
     * Create a new basic entity builder.
     * @param type The entity type.
     * @return The entity builder.
     */
    public static EntityBuilder newBuilder(EntityType type) {
        return new BasicEntityBuilder(type);
    }

    /**
     * Create a new basic entity builder.
     * @param id The entity ID.
     * @return The entity builder.
     */
    public static EntityBuilder newBuilder(EntityType type, long id) {
        return newBuilder(type).setId(id);
    }

    /**
     * Create a new entity builder that is initialized with a copy of an entity.
     * @param e The entity.
     * @return An entity builder initialized to build a copy of {@code e}.
     */
    public static EntityBuilder copyBuilder(Entity e) {
        EntityBuilder eb = newBuilder(e.getType(), e.getId());
        for (TypedName a: e.getTypedAttributeNames()) {
            eb.setAttribute(a, e.get(a));
        }
        return eb;
    }

    /**
     * Create a predicate that filters events for an entity type.
     * @param type The entity type.
     * @return A predicate matching entities of type `type`.
     */
    public static Predicate<Entity> typePredicate(final EntityType type) {
        return new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity input) {
                return (input != null) && input.getType().equals(type);
            }
        };
    }

    /**
     * Create a predicate that filters events for an ID.
     * @param id The ID sought.
     * @return A predicate matching entities with id `id`.
     */
    public static Predicate<Entity> idPredicate(final long id) {
        return new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity input) {
                return (input != null) && input.getId() == id;
            }
        };
    }

    /**
     * Return an ordering over entities that sorts them by ID.
     * @return An ordering over entities by ID.
     */
    public static Ordering<Entity> idOrdering() {
        return ID_ORDER;
    }

    /**
     * Key extractor that keys entities by ID.
     * @return A key extractor that returns entities' IDs.
     */
    public static KeyExtractor<Entity> idKeyExtractor() {
        return ID_KEY_EX;
    }

    public static <T> Function<Entity,T> attributeValueFunction(final TypedName<T> name) {
        return new Function<Entity, T>() {
            @Nullable
            @Override
            public T apply(@Nullable Entity input) {
                return input == null ? null : input.maybeGet(name);
            }
        };
    }

    /**
     * Function that returns entity types' names.
     * @return A function that returns entity types' names.
     */
    public static Function<EntityType,String> entityTypeNameFunction() {
        return new Function<EntityType, String>() {
            @Nullable
            @Override
            public String apply(@Nullable EntityType input) {
                return input == null ? null : input.getName();
            }
        };
    }

    /**
     * Project an entity to a target view type.
     * @param e The entity to project.
     * @param viewClass The view type.
     * @param <E> The view type.
     * @return The projected entity.
     */
    public static <E extends Entity> E project(@Nonnull Entity e, @Nonnull Class<E> viewClass) {
        if (viewClass.isInstance(e)) {
            return viewClass.cast(e);
        } else {
            Optional<Constructor<? extends EntityBuilder>> ctor = BUILDER_CTOR_CACHE.get(viewClass);
            if (ctor == null) {
                BuiltBy bb = viewClass.getAnnotation(BuiltBy.class);
                if (bb != null) {
                    Class<? extends EntityBuilder> ebClass = bb.value();
                    Constructor<? extends EntityBuilder> found =
                            ConstructorUtils.getAccessibleConstructor(ebClass, EntityType.class);
                    ctor = Optional.<Constructor<? extends EntityBuilder>>fromNullable(found);
                } else {
                    ctor = Optional.absent();
                }
                BUILDER_CTOR_CACHE.put(viewClass, ctor);
            }

            if (ctor.isPresent()) {
                EntityBuilder builder = null;
                try {
                    builder = ctor.get().newInstance(e.getType());
                } catch (IllegalAccessException | InstantiationException ex) {
                    throw new VerifyException(ctor.get() + " cannot be instantiated", ex);
                } catch (InvocationTargetException ex) {
                    throw new UncheckedExecutionException("error invoking " + ctor.get(), ex);
                }
                for (Attribute<?> attr: e.getAttributes()) {
                    builder.setAttribute(attr);
                }
                return viewClass.cast(builder.build());
            } else {
                throw new IllegalArgumentException("entity type " + e.getClass() + " cannot be projected to " + viewClass);
            }
        }
    }

    /**
     * Create a projection function that maps entities to a new view.
     * @param viewClass The target view class type.
     * @param <E> The entity type.
     * @return A function that will project entities.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity> Function<Entity,E> projection(final Class<E> viewClass) {
        if (viewClass.equals(Entity.class)) {
            return (Function) Functions.identity();
        } else {
            return n -> project(n, viewClass);
        }
    }

    /**
     * Extract the ID from an entity as its key.
     */
    private static class IdKeyEx implements KeyExtractor<Entity> {
        @Override
        public long getKey(Entity obj) {
            return obj.getId();
        }
    }

    private static class IdOrder extends Ordering<Entity> {
        @Override
        public int compare(@Nullable Entity left, @Nullable Entity right) {
            assert left != null;
            assert right != null;
            return Longs.compare(left.getId(), right.getId());
        }
    }

    private static Ordering<Entity> ID_ORDER = new IdOrder();
    private static KeyExtractor<Entity> ID_KEY_EX = new IdKeyEx();
}
