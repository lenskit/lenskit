/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.Indexer;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.params.meta.Built;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory snapshot of rating data stored in packed arrays.
 * <p>
 * Note that PackedRatingSnapshot is annotated with @Built but is declared as
 * ephemeral. Because of this, the snapshot will not be included in built
 * RecommenderEngines, so it is not Serializable.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@Built(ephemeral = true)
public class PackedRatingSnapshot extends AbstractRatingSnapshot {

    private static final Logger logger = LoggerFactory
            .getLogger(PackedRatingSnapshot.class);
    private PackedRatingData data;
    private List<? extends IntList> userIndices;

    protected PackedRatingSnapshot(PackedRatingData data,
            List<? extends IntList> userIndices) {
        super();
        this.data = data;
        this.userIndices = userIndices;
    }

    private void requireValid() {
        if (data == null)
            throw new IllegalStateException("build context closed");
    }

    @Override
    public LongCollection getUserIds() {
        requireValid();
        return data.userIndex.getIds();
    }

    @Override
    public LongCollection getItemIds() {
        requireValid();
        return data.itemIndex.getIds();
    }

    @Override
    public Index userIndex() {
        requireValid();
        return data.userIndex;
    }

    @Override
    public Index itemIndex() {
        requireValid();
        return data.itemIndex;
    }

    @Override
    public FastCollection<IndexedPreference> getRatings() {
        return new PackedRatingCollection(data);
    }

    @Override
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        requireValid();
        int uidx = data.userIndex.getIndex(userId);
        if (uidx < 0 || uidx >= userIndices.size())
            return CollectionUtils.emptyFastCollection();
        else
            return new PackedRatingCollection(data, userIndices.get(uidx));
    }

    @Override
    public void close() {
        super.close();
        data = null;
    }

    /**
     * A Factory that creates PackedRatingBuildSnapshots from an opened
     * DataAccessObject.
     */
    public static class Builder implements
            org.grouplens.lenskit.Builder<PackedRatingSnapshot> {
        private final DataAccessObject dao;

        public Builder(DataAccessObject dao) {
            this.dao = dao;
        }

        @Override
        public PackedRatingSnapshot build() {
            logger.debug("Packing build context");
            Cursor<Rating> ratings = null;

            // Set up several array lists to accumulate rating data
            IntArrayList users;
            IntArrayList items;
            DoubleArrayList values;

            // We will use item and user indexers to map IDs to indexes
            Indexer itemIndex = new Indexer();
            Indexer userIndex = new Indexer();

            // Track the indices where everything appears for finding previous
            // rating info for a user-item pair
            IndexManager imgr = new IndexManager(2000);

            int nratings = 0;
            // list of indices freed by an unrate event
            IntPriorityQueue free = new IntHeapPriorityQueue();

            try {
                // Since we iterate in timestamp order, we can just overwrite
                // old data for a user-item pair with new data.
                ratings = dao.getEvents(Rating.class, SortOrder.TIMESTAMP);

                // find initial size, defaulting to something nice and large
                int size = ratings.getRowCount();
                if (size < 0)
                    size = 10000;

                users = new IntArrayList(size);
                items = new IntArrayList(size);
                values = new DoubleArrayList(size);
                itemIndex = new Indexer();
                userIndex = new Indexer();

                for (Rating r: ratings.fast()) {
                    final int iidx = itemIndex.internId(r.getItemId());
                    final int uidx = userIndex.internId(r.getUserId());
                    final Preference p = r.getPreference();

                    int index = imgr.getIndex(uidx, iidx);
                    if (index < 0) {
                        // this user-item pair has no rating

                        // skip to next rating if unrate
                        if (p == null)
                            continue;

                        // new rating - find a free index
                        boolean reuse = !free.isEmpty();
                        index = reuse ? free.dequeueInt() : nratings;
                        // remember the index and bump the count
                        imgr.putIndex(uidx, iidx, index);
                        nratings++;

                        // add the new rating data
                        if (reuse) {
                            users.set(index, uidx);
                            users.set(index, iidx);
                            values.set(index, p.getValue());
                        } else {
                            users.add(uidx);
                            items.add(iidx);
                            values.add(p.getValue());
                        }

                        // array sizes should be rating count + unused entries
                        assert users.size() == nratings + free.size();
                        assert items.size() == nratings + free.size();
                        assert values.size() == nratings + free.size();
                    } else {
                        // we have seen this rating before...
                        if (p == null) {
                            // free the entry, no rating here
                            free.enqueue(index);
                            nratings--;
                            // User-item pair? What user-item pair?
                            imgr.putIndex(uidx, iidx, -1);
                        } else {
                            // just overwrite the previous value
                            values.set(index, p.getValue());
                        }
                    }
                }

                logger.debug("Packed {} ratings", nratings);
            } finally {
                if (ratings != null)
                    ratings.close();
            }

            imgr = null;
            if (!free.isEmpty())
                repack(users, items, values, free);

            // at this point, no free slots
            assert users.size() == nratings;
            assert items.size() == nratings;
            assert values.size() == nratings;

            // Blit each list to a new array & clear old array
            // TODO Evaluate if we want to allow wasted space to reduce copying
            int[] itemArray = items.toIntArray();
            items = null;
            int[] userArray = users.toIntArray();
            users = null;
            double[] valueArray = values.toDoubleArray();
            values = null;

            List<? extends IntList> userIndices =
                shuffleAndIndex(userIndex.getObjectCount(),
                                userArray, itemArray, valueArray);

            PackedRatingData data =
                new PackedRatingData(userArray, itemArray, valueArray,
                                     userIndex, itemIndex);
            assert data.users.length == nratings;
            assert data.items.length == nratings;
            assert data.values.length == nratings;
            return new PackedRatingSnapshot(data, userIndices);
        }

        /**
         * Re-pack the data arrays to eliminate free slots. We do this by moving
         * values from the end of the arrays into the free slots. Finally, we
         * trim the arrays to eliminate wasted space.
         *
         * @param users The user array.
         * @param items The item array.
         * @param values The value array.
         * @param free The queue of free slots.
         */
        private void repack(IntArrayList users, IntArrayList items,
                            DoubleArrayList values, IntPriorityQueue free) {
            /*
             * we have to do this backwards so we don't copy free slots from the
             * end of the arrays. So, we first create an array of free indices
             * in reverse order.
             */
            int[] fidxes = new int[free.size()];
            for (int i = free.size() - 1; !free.isEmpty(); i--) {
                fidxes[i] = free.dequeueInt();
            }

            /*
             * We then start with the last free index, and copy into it if it
             * isn't at the end of the array.
             */
            for (int i: fidxes) {
                int lasti = users.size() - 1;
                if (i == users.size()) {
                    users.removeInt(i);
                    items.removeInt(i);
                    values.removeDouble(i);
                } else {
                    users.set(i, users.removeInt(lasti));
                    items.set(i, items.removeInt(lasti));
                    values.set(i, values.removeDouble(lasti));
                }
            }
        }

        /**
         * Shuffle the packed data set, clear free slots, and index the user
         * entries.
         *
         * @param nusers The number of users in the snapshot.
         * @param users The user indices for the packed ratings.
         * @param items The item indices for the packed ratings.
         * @param values The values for the packed ratings.
         * @param freeSlots The slots in the user/item/value lists that are
         *            free.
         * @return A list containing the lists of global indices used by each
         *         user.
         */
        List<? extends IntList> shuffleAndIndex(int nusers, int[] users,
                                                int[] items, double[] values) {
            ArrayList<IntArrayList> userRatingIndex =
                new ArrayList<IntArrayList>(nusers);
            for (int i = 0; i < nusers; i++) {
                userRatingIndex.add(new IntArrayList());
            }

            Random rnd = new Random();

            // do a reverse Fisher-Yates shuffle on the arrays
            final int end = users.length;
            for (int i = 0; i < end; i++) {
                if (i < end - 1) {
                    // pick a j s.t. i <= j < end && j is not free
                    int j = i + rnd.nextInt(end - i);
                    assert j >= i;
                    assert j < end;

                    // swap i with j and index
                    swap(users, i, j);
                    swap(items, i, j);
                    swap(values, i, j);
                }

                // record the rating at [i] in the index
                userRatingIndex.get(users[i]).add(i);
            }

            // trim the user lists
            for (IntArrayList l: userRatingIndex) {
                l.trim();
            }

            return userRatingIndex;
        }

        static void swap(int[] lst, int i, int j) {
            int tmp = lst[i];
            lst[i] = lst[j];
            lst[j] = tmp;
        }

        static void swap(double[] lst, int i, int j) {
            double tmp = lst[i];
            lst[i] = lst[j];
            lst[j] = tmp;
        }
    }
}
