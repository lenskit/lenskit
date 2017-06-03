/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
            return Longs.compare(left.getId(), right.getId());
        }
    }

    private static Ordering<Entity> ID_ORDER = new IdOrder();
    private static KeyExtractor<Entity> ID_KEY_EX = new IdKeyEx();
}
