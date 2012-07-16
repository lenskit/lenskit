/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory snapshot of rating data stored in packed arrays.
 * <p>
 * Note that PackedPreferenceSnapshot is annotated with @Built but is declared as
 * ephemeral. Because of this, the snapshot will not be included in built
 * RecommenderEngines, so it is not Serializable.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@DefaultProvider(PackedPreferenceSnapshot.Provider.class)
public class PackedPreferenceSnapshot extends AbstractPreferenceSnapshot {
    private static final Logger logger = LoggerFactory.getLogger(PackedPreferenceSnapshot.class);

    /**
     * A Factory that creates PackedRatingBuildSnapshots from an opened
     * DataAccessObject.
     */
    public static class Provider implements javax.inject.Provider<PackedPreferenceSnapshot> {
        private final DataAccessObject dao;

        @Inject
        public Provider(@Transient DataAccessObject dao) {
            this.dao = dao;
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
            Cursor<Rating> ratings = dao.getEvents(Rating.class, SortOrder.TIMESTAMP);
            try {
                for (Rating r: ratings.fast()) {
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

            bld.shuffle();
            PackedPreferenceData data = bld.build();

            return new PackedPreferenceSnapshot(data);
        }
    }
    
    private PackedPreferenceData data;
    private volatile List<? extends IntList> userIndices;

    protected PackedPreferenceSnapshot(PackedPreferenceData data) {
        super();
        this.data = data;
    }

    private void requireValid() {
        if (data == null)
            throw new IllegalStateException("build context closed");
    }

    private List<? extends IntList> computeUserIndices() {
        int nusers = data.getUserIndex().getObjectCount();
        ArrayList<IntArrayList> userLists = new ArrayList<IntArrayList>(nusers);
        for (int i = 0; i < nusers; i++) {
            userLists.add(new IntArrayList());
        }
        for (IndexedPreference pref: CollectionUtils.fast(getRatings())) {
            final int uidx = pref.getUserIndex();
            final int idx = pref.getIndex();
            userLists.get(uidx).add(idx);
        }
        for (IntArrayList lst: userLists) {
            lst.trim();
        }
        return userLists;
    }

    private void requireUserIndices() {
        if (userIndices == null) {
            synchronized (this) {
                if (userIndices == null) {
                    userIndices = computeUserIndices();
                }
            }
        }
    }

    @Override
    public LongCollection getUserIds() {
        return userIndex().getIds();
    }

    @Override
    public LongCollection getItemIds() {
        return itemIndex().getIds();
    }

    @Override
    public Index userIndex() {
        requireValid();
        return data.getUserIndex();
    }

    @Override
    public Index itemIndex() {
        requireValid();
        return data.getItemIndex();
    }

    @Override
    public FastCollection<IndexedPreference> getRatings() {
        return new PackedPreferenceCollection(data);
    }

    @Override
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        int uidx = userIndex().getIndex(userId);
        requireUserIndices();
        if (uidx < 0 || uidx >= userIndices.size()) {
            return CollectionUtils.emptyFastCollection();
        } else {
            return new PackedPreferenceCollection(data, userIndices.get(uidx));
        }
    }

    @Override
    public void close() {
        super.close();
        data = null;
    }
}
