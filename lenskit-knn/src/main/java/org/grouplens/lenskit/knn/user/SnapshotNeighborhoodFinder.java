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
package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingPacker;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.UseTimestamps;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;

/**
 * A neighborhood finder that has a snapshot of the rating data for efficiency.  This is built by
 * backing a {@link SimpleNeighborhoodFinder} with a {@link org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(SnapshotNeighborhoodFinder.Builder.class)
public class SnapshotNeighborhoodFinder implements NeighborhoodFinder, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SnapshotNeighborhoodFinder.class);

    private final SimpleNeighborhoodFinder delegate;

    SnapshotNeighborhoodFinder(SimpleNeighborhoodFinder snf) {
        delegate = snf;
    }

    @Override
    public Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(@Nonnull UserHistory<? extends Event> user, @Nonnull LongSet items) {
        return delegate.findNeighbors(user, items);
    }

    public static class Builder implements Provider<SnapshotNeighborhoodFinder> {
        private final EventDAO eventDao;
        private final int neighborhoodSize;
        private final UserSimilarity similarity;
        private final UserVectorNormalizer normalizer;
        private final boolean useTimestamps;

        /**
         * Construct a new snapshot builder.
         * @param dao The DAO.
         * @param nnbrs The number of neighbors to use for each user.
         * @param sim The user similarity function.
         * @param norm The user normalizer.
         * @param useTS Whether to store timestamps in the snapshotted rating data.
         * @todo This is needlessly inefficient when there are rerate events
         */
        @Inject
        public Builder(@Transient EventDAO dao,
                       @NeighborhoodSize int nnbrs,
                       UserSimilarity sim,
                       UserVectorNormalizer norm,
                       @UseTimestamps boolean useTS) {
            eventDao = dao;
            neighborhoodSize = nnbrs;
            similarity = sim;
            normalizer = norm;
            useTimestamps = useTS;
        }

        @Override
        public SnapshotNeighborhoodFinder get() {
            BinaryRatingDAO dao;
            try {
                dao = makeDAO();
            } catch (IOException e) {
                throw new RuntimeException("Error packing ratings", e);
            }
            SimpleNeighborhoodFinder nf;
            nf = new SimpleNeighborhoodFinder(dao, dao, neighborhoodSize, similarity, normalizer);
            return new SnapshotNeighborhoodFinder(nf);
        }

        private BinaryRatingDAO makeDAO() throws IOException {
            File file = File.createTempFile("ratings", ".pack");
            logger.debug("packing ratings to {}", file);
            file.deleteOnExit();
            EnumSet<BinaryFormatFlag> flags = EnumSet.noneOf(BinaryFormatFlag.class);
            if (useTimestamps) {
                flags.add(BinaryFormatFlag.TIMESTAMPS);
            }
            BinaryRatingPacker packer = BinaryRatingPacker.open(file, flags);
            try {
                SortOrder order = useTimestamps ? SortOrder.TIMESTAMP : SortOrder.ANY;
                Cursor<Rating> ratings = eventDao.streamEvents(Rating.class, order);
                try {
                    packer.writeRatings(ratings);
                } finally {
                    ratings.close();
                }
            } finally {
                packer.close();
            }

            BinaryRatingDAO dao = BinaryRatingDAO.open(file);
            // try to delete the file early, helps keep things clean on Unix
            if (file.delete()) {
                logger.debug("unlinked {}, will be deleted when freed", file);
            }
            return dao;
        }
    }
}
