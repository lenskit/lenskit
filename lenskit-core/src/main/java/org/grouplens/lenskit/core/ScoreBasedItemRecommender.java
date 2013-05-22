package org.grouplens.lenskit.core;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.inject.Inject;

/**
 * Old version of {@link TopNItemRecommender}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @deprecated Use {@link TopNItemRecommender} instead.
 */
@Deprecated
public class ScoreBasedItemRecommender extends TopNItemRecommender {
    @Inject
    public ScoreBasedItemRecommender(DataAccessObject dao, ItemScorer scorer) {
        super(dao, scorer);
    }
}
