/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.indexes.IdIndexMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
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
@DefaultProvider(PackedPreferenceSnapshot.Builder.class)
@Shareable
public class PackedPreferenceSnapshot extends AbstractPreferenceSnapshot {
    private static final Logger logger = LoggerFactory.getLogger(PackedPreferenceSnapshot.class);

    /**
     * A Factory that creates PackedRatingBuildSnapshots from an opened
     * DataAccessObject.
     */
    public static class Builder implements Provider<PackedPreferenceSnapshot> {
        private final EventDAO dao;
        private Random random;

        @Inject
        public Builder(@Transient EventDAO dao, Random random) {
            this.dao = dao;
            this.random = random;
        }

        @Override
        public PackedPreferenceSnapshot get() {
            logger.debug("Packing build context");

            PackedPreferenceDataBuilder bld = new PackedPreferenceDataBuilder();

            // Track the indices where everything appears for finding previous
            // rating info for a user-item pair
            Long2ObjectMap<Long2IntMap> uiIndexes =
                    new Long2ObjectOpenHashMap<Long2IntMap>(2000);

            // Since we iterate in timestamp order, we can just overwrite
            // old data for a user-item pair with new data.
            Cursor<Rating> ratings = dao.streamEvents(Rating.class, SortOrder.TIMESTAMP);
            try {
                for (Rating r : ratings.fast()) {
                    final long user = r.getUserId();
                    final long item = r.getItemId();
                    final Preference p = r.getPreference();

                    // get the item -> index map for this user
                    Long2IntMap imap = uiIndexes.get(user);
                    if (imap == null) {
                        imap = new Long2IntOpenHashMap();
                        imap.defaultReturnValue(-1);
                        uiIndexes.put(user, imap);
                    }

                    // have we seen the item?
                    final int index = imap.get(item);
                    if (index < 0) {    // we've never seen (user,item) before
                        // if this is not an unrate (a no-op), add the pref
                        if (p != null) {
                            int idx = bld.add(p);
                            imap.put(item, idx);
                        }
                    } else {            // we have seen this rating before
                        if (p == null) {
                            // free the entry, no rating here
                            bld.release(index);
                            imap.put(item, -1);
                        } else {
                            // just overwrite the previous value
                            bld.set(index, p);
                        }
                    }
                }

                logger.debug("Packed {} ratings", bld.size());
            } finally {
                ratings.close();
            }

            bld.shuffle(random);
            PackedPreferenceData data = bld.build();

            return new PackedPreferenceSnapshot(data);
        }
    }

    public static PreferenceSnapshot pack(EventDAO dao) {
        Builder p = new Builder(dao, new Random());
        return p.get();
    }

    private PackedPreferenceData data;
    private Supplier<List<IntList>> userIndexLists;

    protected PackedPreferenceSnapshot(PackedPreferenceData data) {
        super();
        this.data = data;
        userIndexLists = Suppliers.memoize(new UserIndexListSupplier());
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
    public FastCollection<IndexedPreference> getRatings() {
        return new PackedPreferenceCollection(data);
    }

    @Override
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        int uidx = userIndex().tryGetIndex(userId);
        List<IntList> uidxes = userIndexLists.get();
        if (uidx < 0 || uidx >= uidxes.size()) {
            return CollectionUtils.emptyFastCollection();
        } else {
            return new PackedPreferenceCollection(data, uidxes.get(uidx));
        }
    }

    @Override
    public void close() {
        // FIXME Close is kinda pointless
        super.close();
        data = null;
        userIndexLists = null;
    }

    /**
     * Supplier to create user index lists.  Used to re-use memoization logic.
     */
    private class UserIndexListSupplier implements Supplier<List<IntList>> {
        @Override @Nonnull
        public List<IntList> get() {
            int nusers = data.getUserIndex().size();
            ArrayList<IntList> userLists = new ArrayList<IntList>(nusers);
            for (int i = 0; i < nusers; i++) {
                userLists.add(new IntArrayList());
            }
            for (IndexedPreference pref : CollectionUtils.fast(getRatings())) {
                final int uidx = pref.getUserIndex();
                final int idx = pref.getIndex();
                userLists.get(uidx).add(idx);
            }
            for (IntList lst : userLists) {
                ((IntArrayList) lst).trim();
            }
            return userLists;
        }
    }
}
