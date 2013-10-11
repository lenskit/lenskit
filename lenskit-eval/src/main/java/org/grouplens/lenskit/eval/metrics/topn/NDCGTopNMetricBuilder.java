/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.eval.metrics.topn;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NDCGTopNMetricBuilder implements Builder<NDCGTopNMetric> {
    private String label = "TopN.nDCG";
    private int listSize = 5;
    private ItemSelector candidates = ItemSelectors.testItems();
    private ItemSelector exclude = ItemSelectors.trainingItems();

    /**
     * Get the column label for this metric.
     * @return The column label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the column label for this metric.
     * @param l The column label
     * @return The builder (for chaining).
     */
    public NDCGTopNMetricBuilder setLabel(String l) {
        Preconditions.checkNotNull(l, "label cannot be null");
        label = l;
        return this;
    }


    /**
     * Get the list size.
     * @return The number of items to return in a recommendation list.
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * Get the candidate item selector.
     * @return The candidate item selector.
     */
    public ItemSelector getCandidates() {
        return candidates;
    }

    /**
     * Get the exclude item selector.
     * @return The exclude item selector.
     */
    public ItemSelector getExclude() {
        return exclude;
    }

    /**
     * Set the recommendation list size.  The default length is 10.
     * @param n The recommendation list size.
     * @return The builder (for chaining).
     */
    public NDCGTopNMetricBuilder setListSize(int n) {
        listSize = n;
        return this;
    }

    /**
     * Set the candidate selector.  The default is {@link ItemSelectors#testItems()}.
     * @param sel The candidate item selector.
     * @return The builder (for chaining).
     */
    public NDCGTopNMetricBuilder setCandidates(ItemSelector sel) {
        candidates = sel;
        return this;
    }

    /**
     * Set the exclude item selector.  The default is {@link ItemSelectors#trainingItems()}.
     * @param sel The exclude item selector.
     * @return The builder (for chaining).
     */
    public NDCGTopNMetricBuilder setExclude(ItemSelector sel) {
        exclude = sel;
        return this;
    }

    public NDCGTopNMetric build() {
        return new NDCGTopNMetric(label, listSize, candidates, exclude);
    }
}
