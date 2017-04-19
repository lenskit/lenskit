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
package org.lenskit.data.store;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.*;
import org.lenskit.util.BinarySearch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Packed implementation of the entity collection class.
 */
class PackedEntityCollection extends EntityCollection {
    private final EntityType entityType;
    private final AttributeSet attributes;
    private final LongAttrStore idStore;
    private final AttrStore[] attrStores;
    private final PackIndex[] indexes;
    private final int size;
    private ConcurrentHashMap<Integer,AttributeSet> attrSets = new ConcurrentHashMap<>();

    PackedEntityCollection(EntityType et, AttributeSet attrs, AttrStore[] stores, PackIndex[] idxes) {
        entityType = et;
        attributes = attrs;
        attrStores = stores;
        indexes = idxes;
        idStore = (LongAttrStore) stores[0];
        size = idStore.size();
    }

    @Override
    public EntityType getType() {
        return entityType;
    }

    @Override
    public LongSet idSet() {
        return new IdSet();
    }

    @Nullable
    @Override
    public Entity lookup(long id) {
        int pos = new IdSearch(id).search(0, size);
        if (pos >= 0) {
            return new IndirectEntity(pos);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public <T> List<Entity> find(TypedName<T> name, T value) {
        int idx = attributes.lookup(name);
        if (idx < 0) {
            return ImmutableList.of();
        }

        PackIndex index = indexes[idx];
        if (index != null) {
            return index.getPositions(value)
                        .stream()
                        .map(IndirectEntity::new)
                        .collect(Collectors.toList());
        } else {
            return stream().filter(e -> value.equals(e.maybeGet(name)))
                           .collect(Collectors.toList());
        }
    }

    @Nonnull
    @Override
    public <T> List<Entity> find(Attribute<T> attr) {
        return find(attr.getTypedName(), attr.getValue());
    }

    @Nonnull
    @Override
    public List<Entity> find(String name, Object value) {
        int idx = attributes.lookup(name);
        if (idx < 0) {
            return ImmutableList.of();
        }

        PackIndex index = indexes[idx];
        if (index != null) {
            return index.getPositions(value)
                        .stream()
                        .map(IndirectEntity::new)
                        .collect(Collectors.toList());
        } else {
            return stream().filter(e -> value.equals(e.maybeGet(name)))
                           .collect(Collectors.toList());
        }
    }

    public Stream<Entity> stream() {
        return IntStream.range(0, size)
                        .mapToObj(IndirectEntity::new);
    }

    @Override
    public Iterator<Entity> iterator() {
        return stream().iterator();
    }

    @Override
    public int size() {
        return idStore.size();
    }

    private class IndirectEntity extends AbstractEntity {
        private final int position;

        IndirectEntity(int pos) {
            super(entityType, idStore.getLong(pos));
            position = pos;
        }

        @Override
        public Set<TypedName<?>> getTypedAttributeNames() {
            int missing = 0;
            int na = attributes.size();
            for (int i = 0; i < na; i++) {
                if (attrStores[i].isNull(position)) {
                    missing |= 1 << i;
                }
            }
            if (missing != 0) {
                AttributeSet set = attrSets.get(missing);
                if (set == null) {
                    set = AttributeSet.create(IntStream.range(0, na)
                                                       .filter(i -> !attrStores[i].isNull(position))
                                                       .mapToObj(attributes::getAttribute)
                                                       .collect(Collectors.toList()));
                    attrSets.put(missing, set);
                }
                return set;
            }

            return attributes;
        }

        @Override
        public Collection<Attribute<?>> getAttributes() {
            return new AbstractCollection<Attribute<?>>() {
                @Override
                public Iterator<Attribute<?>> iterator() {
                    return (Iterator) IntStream.range(0, attributes.size())
                                               .mapToObj(i -> {
                                                   Object val = attrStores[i].get(position);
                                                   if (val == null) {
                                                       return null;
                                                   } else {
                                                       return Attribute.create((TypedName) attributes.getAttribute(i), val);
                                                   }
                                               })
                                               .iterator();
                }

                @Override
                public int size() {
                    return attributes.size();
                }
            };
        }

        @Override
        public boolean hasAttribute(String name) {
            int ap = attributes.lookup(name);
            return ap >= 0 && !attrStores[ap].isNull(position);
        }

        @Override
        public boolean hasAttribute(TypedName<?> name) {
            int ap = attributes.lookup(name);
            return ap >= 0 && !attrStores[ap].isNull(position);
        }

        @Nullable
        @Override
        public Object maybeGet(String attr) {
            int ap = attributes.lookup(attr);
            return ap >= 0 ? attrStores[ap].get(position) : null;
        }
    }

    private class IdSearch extends BinarySearch {
        private final long targetId;

        IdSearch(long id) {
            targetId = id;
        }

        @Override
        protected int test(int pos) {
            return Longs.compare(targetId, idStore.getLong(pos));
        }
    }

    private class IdSet extends AbstractLongSet {
        @Override
        public LongIterator iterator() {
            return new IdIter();
        }

        @Override
        public int size() {
            return idStore.size();
        }
    }

    private class IdIter extends AbstractLongIterator {
        int pos = 0;

        @Override
        public long nextLong() {
            if (pos >= idStore.size()) {
                throw new NoSuchElementException();
            }
            return idStore.getLong(pos++);
        }

        @Override
        public boolean hasNext() {
            return pos < idStore.size();
        }
    }
}
