package org.lenskit.space;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SynchronizedIndexSpace implements IndexSpace {
    private final Map<String, ObjectKeyIndex<Object>> keyMap = new HashMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public SynchronizedIndexSpace() {}

    public void requestKeyMap(String name) {
        writeLock.lock();
        try {
            keyMap.put(name, new ObjectKeyIndex<Object>());
        } finally {
            writeLock.unlock();
        }
    }

    public int setKey(String name, Object key) {
        writeLock.lock();
        try {
            return keyMap.get(name).setKey(key);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean containsKey(String name, Object key) {
        readLock.lock();
        try {
            return keyMap.get(name).containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    public int getIndexForKey(String name, Object key) {
        readLock.lock();
        try {
            return keyMap.get(name).getIndex(key);
        } finally {
            readLock.unlock();
        }
    }
}
