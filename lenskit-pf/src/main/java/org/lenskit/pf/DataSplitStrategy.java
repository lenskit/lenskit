package org.lenskit.pf;


import org.lenskit.data.ratings.RatingMatrixEntry;

import java.util.List;

public interface DataSplitStrategy {

    List<RatingMatrixEntry> getTrainingRatings();

    List<RatingMatrixEntry> getValidationRatings();
}
