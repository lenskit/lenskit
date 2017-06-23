package org.lenskit.pf;


import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.KeyIndex;

import java.io.Serializable;
import java.util.List;

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
    public Int2ObjectMap<Int2DoubleMap> getTrainingMatrix() {
        return training;
    }

    @Override
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
