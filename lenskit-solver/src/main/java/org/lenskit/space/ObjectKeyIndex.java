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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.Serializable;

/**
 * A general in-memory back-end for indices, supporting {@link SynchronizedIndexSpace}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ObjectKeyIndex<K> implements Serializable {
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
