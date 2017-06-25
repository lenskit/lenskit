package org.lenskit.pf;


import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.KeyIndex;

import java.io.Serializable;
import java.util.List;

@DefaultProvider(RandomDataSplitStrategyProvider.class)
@Shareable
public class RandomDataSplitStrategy implements DataSplitStrategy, Serializable {
    private static final long serialVersionUID = 2L;

    private final Int2ObjectMap<Int2DoubleMap> training;
    private final List<RatingMatrixEntry> validation;
    private final Int2ObjectMap<ImmutableSet<Integer>> userItemIndices;
    private final KeyIndex userIndex;
    private final KeyIndex itemIndex;

    public RandomDataSplitStrategy(Int2ObjectMap<Int2DoubleMap> train,
                                   List<RatingMatrixEntry> val,
                                   Int2ObjectMap<ImmutableSet<Integer>> userItems,
                                   KeyIndex userInd,
                                   KeyIndex itemInd) {
        training = train;
        validation = val;
        userItemIndices = userItems;
        userIndex = userInd;
        itemIndex = itemInd;
    }
    @Override
    public Int2ObjectMap<Int2DoubleMap> getTrainingMatrix() {
        return training;
    }

    @Override
    public List<RatingMatrixEntry> getValidationRatings() {
        return validation;
    }

    public Int2ObjectMap<ImmutableSet<Integer>> getUserItemIndices() {
        return userItemIndices;
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
