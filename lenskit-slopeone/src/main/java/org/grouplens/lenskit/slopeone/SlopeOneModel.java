/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.util.Index;

/**
 * A model for a <tt>SlopeOneRatingPredictor</tt> or <tt>WeightedSlopeOneRatingPredictor</tt>.
 * Stores calculated deviation values and number of co-rating users for each item pair.
 * Also contains a <tt>BaselinePredictor</tt> and the minimum and maximum rating values
 * for use by a scorer.
 */
@DefaultProvider(SlopeOneModelProvider.class)
@Shareable
public class SlopeOneModel {

    private final Long2IntOpenHashMap[] coMatrix;
    private final Long2DoubleOpenHashMap[] devMatrix;
    private final BaselinePredictor baseline;
    private final Index itemIndex;
    private final PreferenceDomain domain;

    public SlopeOneModel(Long2IntOpenHashMap[] coMatrix,Long2DoubleOpenHashMap[] devMatrix,
    	BaselinePredictor predictor, Index itemIndex, PreferenceDomain dom) {

        this.coMatrix = coMatrix;
        this.devMatrix = devMatrix;
        baseline = predictor;
        this.itemIndex = itemIndex;
        domain = dom;
    }

    public double getDeviation(long item1, long item2) {
        if (item1 == item2) return 0;
        else if (item1 < item2) {
            int index = itemIndex.getIndex(item1);
            if (index < 0) {
                return Double.NaN;
            } else {
                return devMatrix[index].get(item2);
            }
        }
        else {
            int index = itemIndex.getIndex(item2);
            if (index < 0) {
                return Double.NaN;
            } else {
                return -devMatrix[index].get(item1);
            }
        }
    }

    public int getCoratings(long item1, long item2) {
        if (item1 == item2) return 0;
        else if (item1 < item2) {
        	int index = itemIndex.getIndex(item1);
            if (index < 0) {
            	return 0;
            } else {
            	return coMatrix[index].get(item2);
            }
        }
        else {
            int index = itemIndex.getIndex(item2);
            if (index < 0) {
            	return 0;
            } else {
            	return coMatrix[index].get(item1);
            }
        }
    }

    public BaselinePredictor getBaselinePredictor() {
        return baseline;
    }

    public Index getItemIndex() {
        return itemIndex;
    }

    public PreferenceDomain getDomain() {
        return domain;
    }
}
