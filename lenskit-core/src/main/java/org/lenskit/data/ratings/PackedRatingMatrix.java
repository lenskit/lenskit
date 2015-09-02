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
package org.lenskit.data.ratings;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preferences;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * An in-memory snapshot of rating data stored in packed arrays.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(PackedRatingMatrixBuilder.class)
@Shareable
public class PackedRatingMatrix implements RatingMatrix {
    public static RatingMatrix pack(EventDAO dao) {
        PackedRatingMatrixBuilder p = new PackedRatingMatrixBuilder(dao, new Random());
        return p.get();
    }

    private PackedPreferenceData data;
    @SuppressWarnings("deprecation")
    private Supplier<List<Collection<IndexedPreference>>> userIndexLists;
    private transient Long2ObjectMap<SparseVector> cache;

    PackedRatingMatrix(PackedPreferenceData data) {
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
    public Collection<IndexedPreference> getRatings() {
        return new PackedPreferenceCollection(data);
    }

    @Override
    public Collection<IndexedPreference> getUserRatings(long userId) {
        int uidx = userIndex().tryGetIndex(userId);
        List<Collection<IndexedPreference>> userLists = userIndexLists.get();
        if (uidx < 0 || uidx >= userLists.size()) {
            return Collections.emptyList();
        } else {
            return userLists.get(uidx);
        }
    }

    @Override
    public synchronized SparseVector userRatingVector(long userId) {
        // FIXME Don't make this so locky
        if (cache == null) {
            cache = new Long2ObjectOpenHashMap<>();
        }
        SparseVector data = cache.get(userId);
        if (data != null) {
            return data;
        } else {
            Collection<IndexedPreference> prefs = this.getUserRatings(userId);
            data = Preferences.userPreferenceVector(prefs).freeze();
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
    private class UserPreferenceSupplier implements Supplier<List<Collection<IndexedPreference>>> {
        @Override @Nonnull
        public List<Collection<IndexedPreference>> get() {
            int nusers = data.getUserIndex().size();
            ArrayList<IntArrayList> userLists = new ArrayList<IntArrayList>(nusers);
            for (int i = 0; i < nusers; i++) {
                userLists.add(new IntArrayList());
            }
            for (IndexedPreference pref : getRatings()) {
                final int uidx = pref.getUserIndex();
                final int idx = pref.getIndex();
                userLists.get(uidx).add(idx);
            }
            ArrayList<Collection<IndexedPreference>> users =
                    new ArrayList<Collection<IndexedPreference>>(nusers);
            for (IntArrayList list: userLists) {
                list.trim();
                users.add(new PackedPreferenceCollection(data, list));
            }
            return users;
        }
    }
}
