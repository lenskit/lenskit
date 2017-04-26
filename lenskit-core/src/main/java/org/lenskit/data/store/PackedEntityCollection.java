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
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.lenskit.data.dao.SortKey;
import org.lenskit.data.entities.*;
import org.lenskit.util.BinarySearch;
import org.lenskit.util.describe.Describable;
import org.lenskit.util.describe.DescriptionWriter;

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
class PackedEntityCollection extends EntityCollection implements Describable {
    private final EntityType entityType;
    private final AttributeSet attributes;
    private final LongAttrStore idStore;
    private final AttrStore[] attrStores;
    private final PackIndex[] indexes;
    private final int size;
    private transient HashCode contentHash;
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
            return new EntityList(index.getPositions(value));
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
            IntList positions = index.getPositions(value);
            return new EntityList(positions);
        } else {
            return stream().filter(e -> value.equals(e.maybeGet(name)))
                           .collect(Collectors.toList());
        }
    }

    @Override
    public List<SortKey> getSortKeys() {
        return ImmutableList.of(SortKey.create(CommonAttributes.ENTITY_ID));
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", entityType)
                .append("entities", size)
                .build();
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("entity_count", size);
        writer.putList("attributes", attributes);
        if (contentHash == null) {
            Hasher hash = Hashing.md5().newHasher();
            for (int i = 0; i < size; i++) {
                hash.putLong(idStore.getLong(i));
                for (int j = 1; j < attributes.size(); j++) {
                    hash.putInt(Objects.hashCode(attrStores[j].get(i)));
                }
            }
            contentHash = hash.hash();
        }
        writer.putField("content_hash", contentHash);
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
                    return new Iterator<Attribute<?>>() {
                        int i = 0;
                        boolean advanced = false;

                        @Override
                        public boolean hasNext() {
                            if (!advanced) {
                                while (i < attrStores.length && attrStores[i].isNull(position)) {
                                    i++;
                                }
                                advanced = true;
                            }
                            return i < attrStores.length;
                        }

                        @Override
                        public Attribute<?> next() {
                            if (hasNext()) {
                                Object val = attrStores[i].get(position);
                                assert val != null;
                                TypedName t = attributes.getAttribute(i);
                                i += 1;
                                advanced = false;
                                return Attribute.create(t, val);
                            } else {
                                throw new NoSuchElementException();
                            }
                        }
                    };
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

        @Nullable
        @Override
        public <T> T maybeGet(TypedName<T> name) {
            int ap = attributes.lookup(name);
            return ap >= 0 ? (T) name.getRawType().cast(attrStores[ap].get(position)) : null;
        }

        @Override
        public long getLong(TypedName<Long> name) {
            int ap = attributes.lookup(name);
            if (ap < 0) {
                throw new NoSuchAttributeException(name.toString());
            }
            AttrStore as = attrStores[ap];
            if (as.isNull(position)) {
                throw new NoSuchElementException(name.toString());
            }
            assert as instanceof LongAttrStore;
            return ((LongAttrStore) as).getLong(position);
        }
    }

    private class EntityList extends AbstractList<Entity> {
        private final IntList positions;

        EntityList(IntList pss) {
            positions = pss;
        }

        @Override
        public Entity get(int index) {
            return new IndirectEntity(positions.get(index));
        }

        @Override
        public int size() {
            return positions.size();
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
