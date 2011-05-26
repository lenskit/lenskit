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

import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;

/**
 * A model for <tt>SlopeOneRatingPredictor</tt> objects. Stores a
 * <tt>DeviationMatrix</tt>, <tt>CoratingMatrix</tt>, and
 * <tt>BaselinePredictor</tt> for use by the rating predictor.
 *
 */
@Built
@DefaultBuilder (SlopeOneModelBuilder.class)
public class SlopeOneModel {
	
	private final CoratingMatrix coMatrix;
	private final DeviationMatrix devMatrix;
	private final BaselinePredictor baseline;
	private final LongSortedSet itemUniverse;
	
	public SlopeOneModel(CoratingMatrix coData, DeviationMatrix devData, BaselinePredictor predictor, LongSortedSet universe) {
		coMatrix = coData;
		devMatrix = devData;
		baseline = predictor;
		itemUniverse = universe;
	}
	
	public CoratingMatrix getCoratingMatrix() {
		return coMatrix;
	}
	
	public DeviationMatrix getDeviationMatrix() {
		return devMatrix;
	}
	
	public BaselinePredictor getBaselinePredictor() {
		return baseline;
	}
	
	public LongSortedSet getItemUniverse() {
		return itemUniverse;
	}

}