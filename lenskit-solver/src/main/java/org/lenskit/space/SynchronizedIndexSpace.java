/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

package org.lenskit.space;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A synchronized in-memory implementation of index space.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
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
