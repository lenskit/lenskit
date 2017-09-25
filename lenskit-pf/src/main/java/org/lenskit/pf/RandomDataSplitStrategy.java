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
package org.lenskit.pf;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * A implementation of DataSplitStrategy using randomly split rating data into training data and validation
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(RandomDataSplitStrategyProvider.class)
@Shareable
public class RandomDataSplitStrategy implements DataSplitStrategy, Serializable {
    private static final long serialVersionUID = 2L;

    private final Int2ObjectMap<Int2DoubleMap> training;
    private final List<RatingMatrixEntry> validation;
    private final KeyIndex userIndex;
    private final KeyIndex itemIndex;

    public RandomDataSplitStrategy(Int2ObjectMap<Int2DoubleMap> train,
                                   List<RatingMatrixEntry> val,
                                   KeyIndex userInd,
                                   KeyIndex itemInd) {
        training = train;
        validation = val;
        userIndex = userInd;
        itemIndex = itemInd;
    }

    @Override
    @Nonnull
    public Int2ObjectMap<Int2DoubleMap> getTrainingMatrix() {
        return training;
    }

    @Override
    @Nonnull
    public List<RatingMatrixEntry> getValidationRatings() {
        return validation;
    }

    @Override
    public KeyIndex getUserIndex() {
        return userIndex;
    }

    @Override
    public KeyIndex getItemIndex() {
        return itemIndex;
    }
}
