/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
