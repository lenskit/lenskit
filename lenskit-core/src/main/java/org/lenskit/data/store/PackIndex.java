package org.lenskit.data.store;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Index for packed collections.
 */
class PackIndex {
    private final Map<?, IntList> indexMap;

    PackIndex(Map<?,IntList> map) {
        indexMap = map;
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
