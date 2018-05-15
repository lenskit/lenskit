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
import com.google.common.primitives.Longs;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
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
        if (ids != null) {
            ids.add(id);
        }

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
            Arrays.quickSort(0, size, this::compareIds, new SortSwap());
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

    private int compareIds(int k1, int k2) {
        return Longs.compare(idStore.getLong(k1), idStore.getLong(k2));
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
