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
package org.lenskit.data.store;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.ints.IntList;
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
import org.lenskit.util.reflect.InstanceFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Packed implementation of the entity collection class.
 */
class PackedEntityCollection extends EntityCollection implements Describable {
    private final EntityType entityType;
    private final IntFunction<Entity> entityBuilder;
    private final AttributeSet attributes;
    private final LongAttrStore idStore;
    private final AttrStore[] attrStores;
    private final AttrSetter[] storeSetters;
    private final PackIndex[] indexes;
    private final int size;
    private transient HashCode contentHash;
    private ConcurrentHashMap<Integer,AttributeSet> attrSets = new ConcurrentHashMap<>();

    PackedEntityCollection(EntityType et, AttributeSet attrs, AttrStore[] stores, PackIndex[] idxes, Class<? extends EntityBuilder> ebc) {
        entityType = et;
        attributes = attrs;
        attrStores = stores;
        indexes = idxes;
        idStore = (LongAttrStore) stores[0];
        size = idStore.size();

        storeSetters = new AttrSetter[stores.length];
        for (int i = 0; i < stores.length; i++) {
            AttrStore as = stores[i];
            TypedName<?> an = attributes.getAttribute(i);
            if (as instanceof LongAttrStore && an.getRawType().equals(Long.class)) {
                storeSetters[i] = new LongAttrSetter((TypedName) an, (LongAttrStore) as);
            } else if (as instanceof DoubleAttrStore && an.getRawType().equals(Double.class)) {
                storeSetters[i] = new DoubleAttrSetter((TypedName) an, (DoubleAttrStore) as);
            } else {
                storeSetters[i] = new ObjectAttrSetter(an, as);
            }
        }

        if (ebc == null || ebc.equals(BasicEntityBuilder.class)) {
            entityBuilder = IndirectEntity::new;
        } else {
            entityBuilder = new Reconstitutor(ebc);
        }
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
            return entityBuilder.apply(pos);
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
    public Map<Long, List<Entity>> grouped(TypedName<Long> attr) {
        Preconditions.checkArgument(attr != CommonAttributes.ENTITY_ID,
                                    "cannot group by entity ID");
        int idx = attributes.lookup(attr);
        if (idx < 0) {
            return Collections.emptyMap();
        }

        PackIndex index = indexes[idx];
        if (index != null) {
            return index.getValues()
                    .stream()
                    .collect(Collectors.toMap(l -> (Long) l,
                                              l -> new EntityList(index.getPositions(l))));
        } else {
            return stream()
                    .filter(e -> e.hasAttribute(attr))
                    .collect(Collectors.groupingBy(e -> e.getLong(attr)));
        }
    }

    @Override
    public List<SortKey> getSortKeys() {
        return ImmutableList.of(SortKey.create(CommonAttributes.ENTITY_ID));
    }

    public Stream<Entity> stream() {
        return IntStream.range(0, size)
                        .mapToObj(entityBuilder);
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
                        @SuppressWarnings({"rawtypes", "unchecked"})
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
            return entityBuilder.apply(positions.getInt(index));
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

    private class IdIter implements LongIterator {
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

    private class Reconstitutor implements IntFunction<Entity> {
        private final InstanceFactory<EntityBuilder> factory;

        public Reconstitutor(Class<? extends EntityBuilder> ebc) {
            factory = InstanceFactory.fromConstructor(ebc, entityType);
        }

        @Override
        public Entity apply(int position) {
            EntityBuilder eb = factory.newInstance();
            eb.setId(idStore.getLong(position));
            for (int i = 1; i < attributes.size(); i++) {
                storeSetters[i].invoke(eb, position);
            }
            return eb.build();
        }
    }

    private static abstract class AttrSetter {
        abstract void invoke(EntityBuilder eb, int position);
    }

    private static class ObjectAttrSetter extends AttrSetter {
        private final TypedName<?> attrName;
        private final AttrStore attrStore;

        ObjectAttrSetter(TypedName<?> name, AttrStore store) {
            attrName = name;
            attrStore = store;
        }

        @Override
        void invoke(EntityBuilder eb, int position) {
            Object obj = attrStore.get(position);
            if (obj != null) {
                eb.setAttribute((TypedName) attrName, obj);
            }
        }
    }

    private static class LongAttrSetter extends AttrSetter {
        private final TypedName<Long> attrName;
        private final LongAttrStore attrStore;

        LongAttrSetter(TypedName<Long> name, LongAttrStore store) {
            attrName = name;
            attrStore = store;
        }

        @Override
        void invoke(EntityBuilder eb, int position) {
            if (!attrStore.isNull(position)) {
                eb.setLongAttribute(attrName, attrStore.getLong(position));
            }
        }
    }

    private static class DoubleAttrSetter extends AttrSetter {
        private final TypedName<Double> attrName;
        private final DoubleAttrStore attrStore;

        DoubleAttrSetter(TypedName<Double> name, DoubleAttrStore store) {
            attrName = name;
            attrStore = store;
        }

        @Override
        void invoke(EntityBuilder eb, int position) {
            if (!attrStore.isNull(position)) {
                eb.setDoubleAttribute(attrName, attrStore.getDouble(position));
            }
        }
    }
}
