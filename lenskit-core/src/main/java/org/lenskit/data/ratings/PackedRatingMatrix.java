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
package org.lenskit.data.ratings;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An in-memory snapshot of rating data stored in packed arrays.
 */
@DefaultProvider(PackedRatingMatrixProvider.class)
@Shareable
public class PackedRatingMatrix implements RatingMatrix {
    private PackedRatingData data;
    @SuppressWarnings("deprecation")
    private Supplier<List<Collection<RatingMatrixEntry>>> userIndexLists;
    private transient Long2ObjectMap<Long2DoubleMap> cache;

    PackedRatingMatrix(PackedRatingData data) {
        super();
        this.data = data;
        userIndexLists = Suppliers.memoize(new UserPreferenceSupplier());
    }

    private void requireValid() {
        if (data == null) {
            throw new IllegalStateException("build context closed");
        }
    }

    @Override
    public LongCollection getUserIds() {
        return userIndex().getKeyList();
    }

    @Override
    public LongCollection getItemIds() {
        return itemIndex().getKeyList();
    }

    @Override
    public KeyIndex userIndex() {
        requireValid();
        return data.getUserIndex();
    }

    @Override
    public KeyIndex itemIndex() {
        requireValid();
        return data.getItemIndex();
    }

    @Override
    public List<RatingMatrixEntry> getRatings() {
        return new PackedRatingCollection(data);
    }

    @Override
    public Collection<RatingMatrixEntry> getUserRatings(long userId) {
        int uidx = userIndex().tryGetIndex(userId);
        List<Collection<RatingMatrixEntry>> userLists = userIndexLists.get();
        if (uidx < 0 || uidx >= userLists.size()) {
            return Collections.emptyList();
        } else {
            return userLists.get(uidx);
        }
    }

    @Override
    public synchronized Long2DoubleMap getUserRatingVector(long userId) {
        // FIXME Don't make this so locky
        if (cache == null) {
            cache = new Long2ObjectOpenHashMap<>();
        }
        Long2DoubleMap data = cache.get(userId);
        if (data != null) {
            return data;
        } else {
            Collection<RatingMatrixEntry> prefs = this.getUserRatings(userId);
            Long2DoubleMap map = new Long2DoubleOpenHashMap();
            for (RatingMatrixEntry e: prefs) {
                map.put(e.getItemId(), e.getValue());
            }
            data = new Long2DoubleSortedArrayMap(map);
            cache.put(userId, data);
            return data;
        }
    }

    /**
     * Dispose of the internal memory in the packed rating matrix.  It is not necessary to call this method, but it is
     * present to free extra memory references early.
     */
    @PreDestroy
    public void dispose() {
        data = null;
        userIndexLists = null;
    }

    /**
     * Supplier to create user index lists.  Used to re-use memoization logic.
     */
    @SuppressWarnings("deprecation")
    private class UserPreferenceSupplier implements Supplier<List<Collection<RatingMatrixEntry>>> {
        @Override @Nonnull
        public List<Collection<RatingMatrixEntry>> get() {
            int nusers = data.getUserIndex().size();
            ArrayList<IntArrayList> userLists = new ArrayList<>(nusers);
            for (int i = 0; i < nusers; i++) {
                userLists.add(new IntArrayList());
            }
            for (RatingMatrixEntry pref : getRatings()) {
                final int uidx = pref.getUserIndex();
                final int idx = pref.getIndex();
                userLists.get(uidx).add(idx);
            }
            ArrayList<Collection<RatingMatrixEntry>> users = new ArrayList<>(nusers);
            for (IntArrayList list: userLists) {
                list.trim();
                users.add(new PackedRatingCollection(data, list));
            }
            return users;
        }
    }
}
