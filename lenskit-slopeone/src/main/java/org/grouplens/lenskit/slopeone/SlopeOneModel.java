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

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;

/**
 * A model for a <tt>SlopeOneRatingPredictor</tt> or <tt>WeightedSlopeOneRatingPredictor</tt>.
 * Stores calculated deviation values and number of co-rating users for each item pair.
 * Also contains a <tt>BaselinePredictor</tt> and the minimum and maximum rating values
 * for use by a scorer.
 */
@Built
@DefaultBuilder (SlopeOneModelBuilder.class)
public class SlopeOneModel {

	private final Long2ObjectOpenHashMap<Long2IntOpenHashMap> coMatrix;
	private final Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> devMatrix;
	private final BaselinePredictor baseline;
	private final LongSortedSet itemUniverse;
	private final double minRating;
	private final double maxRating;

	public SlopeOneModel(Long2ObjectOpenHashMap<Long2IntOpenHashMap> coData,
			Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> devData, BaselinePredictor predictor,
			LongSortedSet universe, double min, double max) {

		coMatrix = coData;
		devMatrix = devData;
		baseline = predictor;
		itemUniverse = universe;
		minRating = min;
		maxRating = max;
	}

	public double getDeviation(long item1, long item2) {
		if (item1 == item2) return 0;
		else if (item1 < item2) {
			Long2DoubleOpenHashMap map = devMatrix.get(item1);
			if (map == null) return Double.NaN;
			else return map.get(item2);
		}
		else {
			Long2DoubleOpenHashMap map = devMatrix.get(item2);
			if (map == null) return Double.NaN;
			else return -map.get(item1);
		}
	}
	
	public int getCoratings(long item1, long item2) {
		if (item1 == item2) return 0;
		else if (item1 < item2) {
			Long2IntOpenHashMap map = coMatrix.get(item1);
			if (map == null) return 0;
			else return map.get(item2);
		}
		else {
			Long2IntOpenHashMap map = coMatrix.get(item2);
			if (map == null) return 0;
			else return map.get(item1);
		}
	}
	
	public BaselinePredictor getBaselinePredictor() {
		return baseline;
	}

	public LongSortedSet getItemUniverse() {
		return itemUniverse;
	}

	public double getMinRating() {
		return minRating;
	}

	public double getMaxRating() {
		return maxRating;
	}
}