/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Partition ratings into outputs.
 */
class EntityPartitionCrossfoldMethod implements CrossfoldMethod {
    private static final Logger logger = LoggerFactory.getLogger(EntityPartitionCrossfoldMethod.class);

    @Override
    public void crossfold(DataAccessObject input, CrossfoldOutput output, EntityType type) throws IOException {
        final int count = output.getCount();
        logger.info("splitting {} data from {} to {} partitions by ratings", type, input, count);

        // make a list ourselves so we can shuffle it, makeList lists are immutable
        List<Rating> ratings;
        try (ObjectStream<Rating> stream = input.query(type).asType(Rating.class).stream()) {
            ratings = Lists.newArrayList(stream);
        }
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof EntityPartitionCrossfoldMethod) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
