package org.lenskit.util.keys;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedIndexSpace {
    private Map<String, ObjectKeyIndex<Long>> longKeyMap = new HashMap<>();
    private Map<String, ObjectKeyIndex<String>> stringKeyMap = new HashMap<>();

    public SynchronizedIndexSpace() {}

    public synchronized void requestStringKeyMap(String name) {
        stringKeyMap.put(name, new ObjectKeyIndex<String>());
    }

    public synchronized void requestLongKeyMap(String name) {
        longKeyMap.put(name, new ObjectKeyIndex<Long>());
    }
}
