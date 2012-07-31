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

import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.util.Index;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.util.Indexer;

public class SlopeOneModelDataAccumulator {

    private Long2DoubleOpenHashMap[] deviationMatrix;
    private Long2IntOpenHashMap[] coratingMatrix;
    private double damping;
    private Index itemIndex;

    /**
     * Creates an accumulator to process rating data and generate the necessary data for
     * a <tt>SlopeOneRatingPredictor</tt>.
     * @param damping A damping term for deviation calculations.
     * @param itemIndex An Index for items in the model
     * @param dao The DataAccessObject interfacing with the data for the model
     */
    public SlopeOneModelDataAccumulator(double damping, Indexer itemIndex, DataAccessObject dao) {
        this.damping = damping;
        this.itemIndex = itemIndex;
        long[] items = Cursors.makeList(dao.getItems()).elements();
        deviationMatrix = new Long2DoubleOpenHashMap[items.length];
        coratingMatrix = new Long2IntOpenHashMap[items.length];
        for (long itemId : items) {
            deviationMatrix[itemIndex.internId(itemId)] = new Long2DoubleOpenHashMap();
            coratingMatrix[itemIndex.internId(itemId)] = new Long2IntOpenHashMap();
        }
        for (int i = 0; i < items.length-1; i++) {
            for (int j = i; j < items.length; j++) {
                // to profit from matrix symmetry, always store by minId
                long minId = Math.min(items[i], items[j]);
                long maxId = Math.max(items[i], items[j]);
                deviationMatrix[itemIndex.getIndex(minId)].put(maxId, Double.NaN);
            }
        }
    }

    /**
     * Provide a pair of ratings to the accumulator.
     * @param id1 The id of the first item.
     * @param rating1 The user's rating of the first item.
     * @param id2 The id of the second item.
     * @param rating2 The user's rating of the second item.
     */
    public void putRatingPair(long id1, double rating1, long id2, double rating2) {
        if (id1 < id2) {
            double currentDeviation = deviationMatrix[itemIndex.getIndex(id1)].get(id2);
            currentDeviation = Double.isNaN(currentDeviation) ? 0.0 : currentDeviation;
            deviationMatrix[itemIndex.getIndex(id1)].put(id2,
                    currentDeviation + (rating1 - rating2));
            int currentCoratings = coratingMatrix[itemIndex.getIndex(id1)].get(id2);
            coratingMatrix[itemIndex.getIndex(id1)].put(id2, currentCoratings + 1);
        }
    }

    /**
     * @return A matrix of item deviation values to be used by
     * a <tt>SlopeOneRatingPredictor</tt>.
     */
    public Long2DoubleOpenHashMap[] buildDeviationMatrix() {
        for (int i = 0; i < coratingMatrix.length; i++) {
            LongIterator itemIter = coratingMatrix[i].keySet().iterator();
    		while (itemIter.hasNext()) {
    			long curItem = itemIter.nextLong();
                if (coratingMatrix[i].get(curItem) != 0) {
                    double deviation = deviationMatrix[i].get(curItem)/(coratingMatrix[i].get(curItem) + damping);
                    deviationMatrix[i].put(curItem, deviation);
                }
            }
        }
        return deviationMatrix;
    }

    /**
     * @return A matrix, containing the number of co-rating users for each item
     * pair, to be used by a <tt>SlopeOneRatingPredictor</tt>.
     */
    public Long2IntOpenHashMap[] buildCoratingMatrix() {
        return coratingMatrix;
    }
}
