/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Collection;

import org.grouplens.lenskit.AbstractDynamicRatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * A <tt>RatingPredictor<tt> that implements the Slope One algorithm.
 */
public class SlopeOneRatingPredictor extends AbstractDynamicRatingPredictor {

	protected SlopeOneModel model;

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
					int nusers = model.getCoratingMatrix().get(predicteeItem, currentItem);
					if (nusers != 0) {
						double currentDev = model.getDeviationMatrix().get(predicteeItem, currentItem);
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
		//Use Baseline Predictor if necessary
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
	
	public LongSet getPredictableItems(long user, SparseVector ratings) {
		if (model.getBaselinePredictor() != null) return model.getItemUniverse();
		else {
			LongSet predictable = new LongOpenHashSet();
			for (long id1 : model.getItemUniverse()) {
				LongIterator iter = ratings.keySet().iterator();
				int nusers = 0;
				while (iter.hasNext() && nusers == 0) {
					nusers += model.getCoratingMatrix().get(id1, iter.next());
				}
				if (nusers > 0) predictable.add(id1);
			}
			return predictable;
		}		
	}
}