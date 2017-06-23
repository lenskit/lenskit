package org.lenskit.pf;


import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.util.keys.KeyIndex;

import java.util.List;

public interface DataSplitStrategy {

    Int2ObjectMap<Int2DoubleMap> getTrainingMatrix();

    List<RatingMatrixEntry> getValidationRatings();

    KeyIndex getUserIndex();

    KeyIndex getItemIndex();
}
