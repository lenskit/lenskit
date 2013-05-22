package org.grouplens.lenskit.core;

import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.TopNGlobalItemRecommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.inject.Inject;

/**
 * Old version of {@link TopNGlobalItemRecommender}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @deprecated Use {@link TopNGlobalItemRecommender} instead.
 */
@Deprecated
public class ScoreBasedGlobalItemRecommender extends TopNGlobalItemRecommender {
    @Inject
    public ScoreBasedGlobalItemRecommender(DataAccessObject dao, GlobalItemScorer scorer) {
        super(dao, scorer);
    }
}
