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

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.*;
import org.lenskit.util.BinarySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity collection builder packing data into shards.
 */
class PackedEntityCollectionBuilder extends EntityCollectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PackedEntityCollectionBuilder.class);
    private final EntityType entityType;
    private final AttributeSet attributes;
    private final LongAttrStoreBuilder idStore;
    private final AttrStoreBuilder[] storeBuilders;
    private final Class<? extends EntityBuilder> entityBuilderClass;
    private boolean needIndex[];
    private LongSet ids = null;
    private boolean isSorted = true;
    private int size = 0;
    private long lastEntityId = Long.MIN_VALUE;

    PackedEntityCollectionBuilder(EntityType et, AttributeSet attrs, Class<? extends EntityBuilder> ebc) {
        Preconditions.checkArgument(attrs.size() > 0, "attribute set is emtpy");
        Preconditions.checkArgument(attrs.size() < 32, "cannot have more than 31 attributes");
        Preconditions.checkArgument(attrs.getAttribute(0) == CommonAttributes.ENTITY_ID,
                                    "attribute set does not contain entity ID attribute");
        entityType = et;
        attributes = attrs;
        int n = attrs.size();
        storeBuilders = new AttrStoreBuilder[n];
        needIndex = new boolean[n];
        idStore = new LongAttrStoreBuilder();
        storeBuilders[0] = idStore;
        for (int i = 1; i < n; i++) {
            TypedName<?> attr = attrs.getAttribute(i);
            AttrStoreBuilder asb;
            if (attr.getType().equals(TypeToken.of(Long.class))) {
                logger.debug("{}: storing  long column {}", et, attr.getName());
                asb = new LongAttrStoreBuilder();
            } else if (attr.getType().equals(TypeToken.of(Integer.class))) {
                logger.debug("{}: storing int column {}", et, attr.getName());
                asb = new AttrStoreBuilder(IntShard::create);
            } else if (attr.getType().equals(TypeToken.of(Double.class))) {
                logger.debug("{}: storing double column {}", et, attr.getName());
                asb = new DoubleAttrStoreBuilder();
            } else {
                logger.debug("{}: storing object column {}", et, attr);
                asb = new AttrStoreBuilder(ObjectShard::new);
            }
            storeBuilders[i] = asb;
        }

        entityBuilderClass = ebc;

    }

    @Override
    public <T> EntityCollectionBuilder addIndex(TypedName<T> attribute) {
        int pos = attributes.lookup(attribute);
        if (pos >= 0) {
            needIndex[pos] = true;
        }
        return this;
    }

    @Override
    public EntityCollectionBuilder addIndex(String attrName) {
        int pos = attributes.lookup(attrName);
        if (pos >= 0) {
            needIndex[pos] = true;
        }
        return this;
    }

    private PackIndex buildIndex(int aidx) {
        TypedName<?> tn = attributes.getAttribute(aidx);
        logger.debug("indexing column {} of {}", tn, entityType);
        PackIndex.Builder builder;
        if (tn.getRawType().equals(Long.class)) {
            builder = new PackIndex.LongBuilder();
        } else {
            builder = new PackIndex.GenericBuilder();
        }
        for (int i = 0; i < size; i++) {
            builder.add(storeBuilders[aidx].get(i), i);
        }
        return builder.build();
    }

    @Override
    public EntityCollectionBuilder add(Entity e, boolean replace) {
        long id = e.getId();
        isSorted &= id > lastEntityId;

        if (!isSorted) {
            if (ids == null) {
                ids = new LongOpenHashSet();
                for (int i = 0; i < size; i++) {
                    ids.add((long) storeBuilders[0].get(i));
                }
            }

            if (ids.contains(id)) {
                if (replace) {
                    throw new UnsupportedOperationException("packed builder cannot replace entities");
                } else {
                    return this; // don't replace existing id
                }
            }
        } else if (!replace) {
            BinarySearch search = new IdSearch(id);
            int res = search.search(0, size);
            if (res <= 0) {
                return this;
            }
        }

        for (Attribute<?> a: e.getAttributes()) {
            int ap = attributes.lookup(a.getTypedName());
            if (ap >= 0) {
                storeBuilders[ap].add(a.getValue());
            }
        }
        size += 1;
        lastEntityId = id;

        for (AttrStoreBuilder storeBuilder : storeBuilders) {
            if (storeBuilder.size() < size) {
                assert storeBuilder.size() == size - 1;
                storeBuilder.skip();
            }
        }

        return this;
    }

    @Override
    public Iterable<Entity> entities() {
        AttrStore[] stores = new AttrStore[storeBuilders.length];
        for (int i = 0; i < stores.length; i++) {
            stores[i] = storeBuilders[i].tempBuild();
        }
        // the packed collection is not fully functional! But it will be iterable.
        return new PackedEntityCollection(entityType, attributes, stores, new PackIndex[attributes.size()], entityBuilderClass);
    }

    @Override
    public EntityCollection build() {
        if (!isSorted) {
            Arrays.quickSort(0, size, new IdComparator(), new SortSwap());
        }
        AttrStore[] stores = new AttrStore[storeBuilders.length];
        PackIndex[] indexes = new PackIndex[needIndex.length];
        for (int i = 0; i < stores.length; i++) {
            stores[i] = storeBuilders[i].build();
            if (needIndex[i]) {
                indexes[i] = buildIndex(i);
            }
        }
        return new PackedEntityCollection(entityType, attributes, stores, indexes, entityBuilderClass);
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

    private class IdComparator extends AbstractIntComparator {
        @Override
        public int compare(int k1, int k2) {
            return Longs.compare(idStore.getLong(k1), idStore.getLong(k2));
        }
    }

    private class SortSwap implements Swapper {
        @Override
        public void swap(int a, int b) {
            for (AttrStoreBuilder asb: storeBuilders) {
                asb.swap(a, b);
            }
        }
    }
}
