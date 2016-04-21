package org.lenskit.space;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedIndexSpace implements IndexSpace {
    private final Map<String, ObjectKeyIndex<Long>> longKeyMap = new HashMap<>();
    private final Map<String, ObjectKeyIndex<String>> stringKeyMap = new HashMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public SynchronizedIndexSpace() {}

    public void requestStringKeyMap(String name) {
        writeLock.lock();
        try {
            stringKeyMap.put(name, new ObjectKeyIndex<String>());
        } finally {
            writeLock.unlock();
        }
    }

    public void requestLongKeyMap(String name) {
        writeLock.lock();
        try {
            longKeyMap.put(name, new ObjectKeyIndex<Long>());
        } finally {
            writeLock.unlock();
        }
    }

    public int setLongKey(String name, long key) {
        writeLock.lock();
        try {
            return longKeyMap.get(name).setKey(key);
        } finally {
            writeLock.unlock();
        }
    }

    public int setStringKey(String name, String key) {
        writeLock.lock();
        try {
            return stringKeyMap.get(name).setKey(key);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean containsLongKey(String name, long key) {
        readLock.lock();
        try {
            return longKeyMap.get(name).containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsStringKey(String name, String key) {
        readLock.lock();
        try {
            return stringKeyMap.get(name).containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    public int getIndexForLongKey(String name, long key) {
        readLock.lock();
        try {
            return longKeyMap.get(name).getIndex(key);
        } finally {
            readLock.unlock();
        }
    }

    public int getIndexForStringKey(String name, String key) {
        readLock.lock();
        try {
            return stringKeyMap.get(name).getIndex(key);
        } finally {
            readLock.unlock();
        }
    }
}
