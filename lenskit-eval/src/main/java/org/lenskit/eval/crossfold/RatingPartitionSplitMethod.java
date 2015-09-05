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
package org.lenskit.eval.crossfold;

import org.lenskit.util.io.ObjectStreams;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.source.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Partition ratings into outputs.
 */
class RatingPartitionSplitMethod implements SplitMethod {
    private static final Logger logger = LoggerFactory.getLogger(RatingPartitionSplitMethod.class);

    @Override
    public void crossfold(DataSource input, CrossfoldOutput output) throws IOException {
        final int count = output.getCount();
        logger.info("splitting data source {} to {} partitions by ratings",
                    input.getName(), count);
        ArrayList<Rating> ratings = ObjectStreams.makeList(input.getEventDAO().streamEvents(Rating.class));
        Collections.shuffle(ratings);

        final int n = ratings.size();
        for (int i = 0; i < n; i++) {
            for (int f = 0; f < count; f++) {
                int foldNum = i % count;
                if (f == foldNum) {
                    output.getTestWriter(f).writeRating(ratings.get(i));
                } else {
                    output.getTrainWriter(f).writeRating(ratings.get(i));
                }
            }
        }
    }
}
