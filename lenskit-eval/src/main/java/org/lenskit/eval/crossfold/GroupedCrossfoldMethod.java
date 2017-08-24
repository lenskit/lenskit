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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GroupedCrossfoldMethod implements CrossfoldMethod {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final EntityType groupType;
    protected final TypedName<Long> groupAttribute;
    private final GroupEntitySplitter entitySplitter;
    protected final SortOrder order;
    protected final HistoryPartitionMethod partition;

    GroupedCrossfoldMethod(EntityType typ, TypedName<Long> attr, GroupEntitySplitter es, SortOrder ord, HistoryPartitionMethod pa) {
        groupType = typ;
        groupAttribute = attr;
        entitySplitter = es;
        order = ord;
        partition = pa;
    }

    @Override
    public void crossfold(DataAccessObject input, CrossfoldOutput output, EntityType type) throws IOException {
        final int count = output.getCount();
        logger.info("splitting {} data from {} to {} partitions by users with method {}",
                    input, input, count, partition);
        Long2IntMap splits = entitySplitter.splitEntities(input.getEntityIds(groupType), count, output.getRandom());
        splits.defaultReturnValue(-1); // unpartitioned users should only be trained
        try (ObjectStream<IdBox<List<Rating>>> userStream = input.query(type)
                                                                 .asType(Rating.class)
                                                                 .groupBy(groupAttribute)
                                                                 .stream()) {
            for (IdBox<List<Rating>> history : userStream) {
                int foldNum = splits.get(history.getId());
                List<Rating> ratings = new ArrayList<>(history.getValue());
                final int n = ratings.size();

                for (int f = 0; f < count; f++) {
                    if (f == foldNum) {
                        order.apply(ratings, output.getRandom());
                        final int p = partition.partition(ratings);
                        for (int j = 0; j < p; j++) {
                            output.getTrainWriter(f).writeRating(ratings.get(j));
                        }
                        for (int j = p; j < n; j++) {
                            output.getTestWriter(f).writeRating(ratings.get(j));
                        }
                    } else {
                        for (Rating rating : ratings) {
                            output.getTrainWriter(f).writeRating(rating);
                        }
                    }
                }

            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GroupedCrossfoldMethod that = (GroupedCrossfoldMethod) o;

        return new EqualsBuilder()
                .append(groupType, that.groupType)
                .append(groupAttribute, that.groupAttribute)
                .append(entitySplitter, that.entitySplitter)
                .append(order, that.order)
                .append(partition, that.partition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(groupType)
                .append(groupAttribute)
                .append(entitySplitter)
                .append(order)
                .append(partition)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entity", groupType)
                .append("attribute", groupAttribute)
                .append("splitter", entitySplitter)
                .append("order", order)
                .append("partition", partition)
                .toString();
    }
}
