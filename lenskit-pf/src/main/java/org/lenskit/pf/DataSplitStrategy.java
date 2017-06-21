package org.lenskit.pf;


import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.util.keys.KeyIndex;

import java.util.List;

public interface DataSplitStrategy {

    List<RatingMatrixEntry> getTrainingRatings();

    List<RatingMatrixEntry> getValidationRatings();

    KeyIndex userIndex();

    KeyIndex itemIndex();
}
