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
package org.grouplens.lenskit.data.snapshot;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.indexes.IdIndexMapping;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An in-memory snapshot of rating data stored in packed arrays.
 * <p>
 * Note that PackedPreferenceSnapshot is annotated with @Built but is declared as
 * ephemeral. Because of this, the snapshot will not be included in built
 * RecommenderEngines, so it is not Serializable.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(PackedPreferenceSnapshotBuilder.class)
@Shareable
public class PackedPreferenceSnapshot extends AbstractPreferenceSnapshot {
    public static PreferenceSnapshot pack(EventDAO dao) {
        PackedPreferenceSnapshotBuilder p = new PackedPreferenceSnapshotBuilder(dao, new Random());
        return p.get();
    }

    private PackedPreferenceData data;
    @SuppressWarnings("deprecation")
    private Supplier<List<FastCollection<IndexedPreference>>> userIndexLists;

    PackedPreferenceSnapshot(PackedPreferenceData data) {
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
        return userIndex().getIdList();
    }

    @Override
    public LongCollection getItemIds() {
        return itemIndex().getIdList();
    }

    @Override
    public IdIndexMapping userIndex() {
        requireValid();
        return data.getUserIndex();
    }

    @Override
    public IdIndexMapping itemIndex() {
        requireValid();
        return data.getItemIndex();
    }

    @Override
    @SuppressWarnings("deprecation")
    public FastCollection<IndexedPreference> getRatings() {
        return new PackedPreferenceCollection(data);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        int uidx = userIndex().tryGetIndex(userId);
        List<FastCollection<IndexedPreference>> userLists = userIndexLists.get();
        if (uidx < 0 || uidx >= userLists.size()) {
            return CollectionUtils.emptyFastCollection();
        } else {
            return userLists.get(uidx);
        }
    }

    @Override
    public void close() {
        // FIXME Close is never called, because there is no lifecycle support.
        super.close();
        data = null;
        userIndexLists = null;
    }

    /**
     * Supplier to create user index lists.  Used to re-use memoization logic.
     */
    @SuppressWarnings("deprecation")
    private class UserPreferenceSupplier implements Supplier<List<FastCollection<IndexedPreference>>> {
        @Override @Nonnull
        public List<FastCollection<IndexedPreference>> get() {
            int nusers = data.getUserIndex().size();
            ArrayList<IntArrayList> userLists = new ArrayList<IntArrayList>(nusers);
            for (int i = 0; i < nusers; i++) {
                userLists.add(new IntArrayList());
            }
            for (IndexedPreference pref : CollectionUtils.fast(getRatings())) {
                final int uidx = pref.getUserIndex();
                final int idx = pref.getIndex();
                userLists.get(uidx).add(idx);
            }
            ArrayList<FastCollection<IndexedPreference>> users =
                    new ArrayList<FastCollection<IndexedPreference>>(nusers);
            for (IntArrayList list: userLists) {
                list.trim();
                users.add(new PackedPreferenceCollection(data, list));
            }
            return users;
        }
    }
}
