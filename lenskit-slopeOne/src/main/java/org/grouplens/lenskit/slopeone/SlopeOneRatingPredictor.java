package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Collection;
import org.grouplens.lenskit.AbstractDynamicRatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

public class SlopeOneRatingPredictor extends AbstractDynamicRatingPredictor {

	protected final SlopeOneModel model;

	public SlopeOneRatingPredictor(RatingDataAccessObject dao, SlopeOneModel model) {
		super(dao);
		this.model = model;
	}

	@Override
	public SparseVector predict(long user, SparseVector ratings, Collection<Long> items) {

		LongSortedSet iset;
		if (items instanceof LongSortedSet)
			iset = (LongSortedSet) items;
		else
			iset = new LongSortedArraySet(items);
		MutableSparseVector preds = new MutableSparseVector(iset, Double.NaN);
		LongArrayList unpreds = new LongArrayList();
		for (long predicteeItem : items) {
			if (!ratings.containsKey(predicteeItem)) {
				double total = 0;
				int nitems = 0;
				for (long currentItem : ratings.keySet()) {
					double currentDev = model.getDeviationMatrix().get(predicteeItem, currentItem);	
					if (!Double.isNaN(currentDev)) {
						total += currentDev + ratings.get(currentItem);
						nitems++;
					}
				}
				if (nitems == 0) unpreds.add(predicteeItem);
				else {
					preds.set(predicteeItem, total/nitems);
				}
			}
		}
		final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null && !unpreds.isEmpty()) {
        	SparseVector basePreds = baseline.predict(user, ratings, unpreds);
            for (Long2DoubleMap.Entry e: basePreds.fast()) {
                assert Double.isNaN(preds.get(e.getLongKey()));
                preds.set(e.getLongKey(), e.getDoubleValue());
            }
            return preds;
        }
        else return preds.copy(true);
	}
}