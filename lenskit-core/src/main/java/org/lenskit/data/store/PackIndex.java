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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Index for packed collections.
 */
class PackIndex {
    private final Map<?, IntList> indexMap;

    PackIndex(Map<?,IntList> map) {
        indexMap = map;
    }

    Set<?> getValues() {
        return indexMap.keySet();
    }

    IntList getPositions(Object value) {
        IntList res = indexMap.get(value);
        if (res == null) {
            return IntLists.EMPTY_LIST;
        } else {
            return res;
        }
    }

    interface Builder {
        void add(Object value, int idx);

        /**
         * Build the index. The state of the builder is undefined after this operation.
         * @return The new index.s
         */
        PackIndex build();
    }

    static class GenericBuilder implements Builder {
        Map<Object,IntArrayList> index = new HashMap<>();

        @Override
        public void add(Object value, int idx) {
            IntArrayList list = index.computeIfAbsent(value, k -> new IntArrayList());
            list.add(idx);
        }

        @Override
        public PackIndex build() {
            Map<Object, IntList> map = index.entrySet()
                                            .stream()
                                            .collect(Collectors.toMap(Map.Entry::getKey,
                                                                      e -> {
                                                                          e.getValue().trim();
                                                                          return e.getValue();
                                                                      }));
            index.clear();
            return new PackIndex(map);
        }
    }

    static class LongBuilder implements Builder {
        Long2ObjectMap<IntArrayList> index = new Long2ObjectOpenHashMap<>();

        @Override
        public void add(Object value, int idx) {
            long v = (long) value;
            IntArrayList list = index.computeIfAbsent(v, k -> new IntArrayList());
            list.add(idx);
        }

        @Override
        public PackIndex build() {
            Long2ObjectMap<IntList> map = new Long2ObjectOpenHashMap<>();
            for (Long2ObjectMap.Entry<IntArrayList> e: index.long2ObjectEntrySet()) {
                e.getValue().trim();
                map.put(e.getLongKey(), e.getValue());
            }
            index.clear();
            return new PackIndex(map);
        }
    }
}
