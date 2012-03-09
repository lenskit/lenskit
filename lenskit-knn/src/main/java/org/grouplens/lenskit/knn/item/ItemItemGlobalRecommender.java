package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.core.ScoreBasedGlobalItemRecommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * Global recommendation with item-item CF.
 * 
 * @author Shuo Chang <schang@cs.umn.edu>
 * 
 */
public class ItemItemGlobalRecommender extends ScoreBasedGlobalItemRecommender {
	protected final ItemItemModelBackedGlobalScorer scorer;

	public ItemItemGlobalRecommender(DataAccessObject dao,
                                     ItemItemModelBackedGlobalScorer scorer) {
		super(dao, scorer);
		this.scorer = scorer;
	}
	
    @Override
    public LongSet getPredictableItems(LongSet items) {
        return scorer.getScoreableItems(items);
    }

}
