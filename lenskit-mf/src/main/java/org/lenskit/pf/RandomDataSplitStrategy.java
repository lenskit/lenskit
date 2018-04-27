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
package org.lenskit.pf;

import com.google.common.collect.ImmutableList;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * An implementation of DataSplitStrategy using randomly split rating data into training data and validation
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(RandomDataSplitStrategyProvider.class)
@Shareable
public class RandomDataSplitStrategy implements DataSplitStrategy, Serializable {
    private static final long serialVersionUID = 2L;

    private final List<RatingMatrixEntry> training;
    private final List<RatingMatrixEntry> validation;
    private final KeyIndex userIndex;
    private final KeyIndex itemIndex;

    public RandomDataSplitStrategy(List<RatingMatrixEntry> train,
                                   List<RatingMatrixEntry> val,
                                   KeyIndex userInd,
                                   KeyIndex itemInd) {
        training = ImmutableList.copyOf(train);
        validation = ImmutableList.copyOf(val);
        userIndex = userInd.frozenCopy();
        itemIndex = itemInd.frozenCopy();
    }

    @Override
    @Nonnull
    public List<RatingMatrixEntry> getTrainRatings() {
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
