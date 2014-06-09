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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Random;

/**
 * A Factory that creates PackedRatingBuildSnapshots from an opened
 * DataAccessObject.
 */
public class PackedPreferenceSnapshotBuilder implements Provider<PackedPreferenceSnapshot> {
    private static final Logger logger = LoggerFactory.getLogger(PackedPreferenceSnapshotBuilder.class);

    private final EventDAO dao;
    private Random random;

    @Inject
    public PackedPreferenceSnapshotBuilder(@Transient EventDAO dao, Random random) {
        this.dao = dao;
        this.random = random;
    }

    @Override
    public PackedPreferenceSnapshot get() {
        logger.debug("Packing preference snapshot");

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
