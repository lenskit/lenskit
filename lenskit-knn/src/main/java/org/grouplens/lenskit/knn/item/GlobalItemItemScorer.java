package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.AbstractGlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Score items based on the basket of items using an item-item CF model.
 * 
 * @author Shuo Chang <schang@cs.umn.edu>
 * 
 */
public class GlobalItemItemScorer extends AbstractGlobalItemScorer implements
		GlobalItemItemModelBackedScorer {
	protected final ItemItemModel model;
	protected final int neighborhoodSize;
	protected @Nonnull NeighborhoodScorer scorer;

	public GlobalItemItemScorer(DataAccessObject dao, ItemItemModel m,
			@NeighborhoodSize int nnbrs) {
		super(dao);
		model = m;
		neighborhoodSize = nnbrs;
		// The global item scorer use the SimilaritySumNeighborhoodScorer for the unary ratings
		this.scorer = new SimilaritySumNeighborhoodScorer();
		
	}

    @Override
    public ItemItemModel getModel() {
        return model;
    }
    
	@Override
	public SparseVector globalScore(Collection<Long> queryItems,
			Collection<Long> items) {
		// create the unary rating for the items
		double[] ratings = new double[queryItems.size()];
		for(int i = 0; i < ratings.length; i++)
			ratings[i] = 0.0;
		long[] ids = new long[queryItems.size()];
		int i = 0;
		for(Long id:queryItems)
			ids[i++] = id.longValue();
		// create a dummy user vector with user id = 0
		UserVector basket = new UserVector(0, ids, ratings, queryItems.size());
		
        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }
        
        MutableSparseVector preds = model.scoreItems(basket, iset, scorer,
        		neighborhoodSize);
		
		return preds.freeze();
	}
	

	@Override
	public LongSet getScoreableItems(Collection<Long> queryItems) {
        // FIXME This method incorrectly assumes the model is symmetric
        LongSet items = new LongOpenHashSet();
        Iterator<Long> iter = queryItems.iterator();
        while (iter.hasNext()) {
            final long item = iter.next().longValue();
            items.addAll(model.getNeighbors(item));
        }
        return items;
    }

}
