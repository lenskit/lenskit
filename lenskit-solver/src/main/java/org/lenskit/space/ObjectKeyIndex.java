package org.lenskit.space;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ObjectKeyIndex<K> {
    private Object2IntOpenHashMap<K> key2idx;
    private ObjectArrayList<K> keyList;

    public ObjectKeyIndex() {
        this.key2idx = new Object2IntOpenHashMap<>();
        this.keyList = new ObjectArrayList<>();
    }

    public int getIndex(K key) {
        return key2idx.get(key);
    }

    public K getKey(int idx) {
        return keyList.get(idx);
    }

    public boolean containsKey(K key) {
        return key2idx.containsKey(key);
    }

    public int size() {
        return keyList.size();
    }

    public int setKey(K key) {
        if (key2idx.containsKey(key)) {
            return key2idx.get(key);
        } else {
            int idx = keyList.size();
            key2idx.put(key, idx);
            keyList.add(key);
            return idx;
        }
    }
}
