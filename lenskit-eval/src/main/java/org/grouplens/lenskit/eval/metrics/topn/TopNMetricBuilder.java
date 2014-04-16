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

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.metrics.Metric;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class TopNMetricBuilder <T extends TopNMetricBuilder, K extends Metric> implements Builder<K> {
    protected int listSize = 5;
    protected ItemSelector candidates = ItemSelectors.testItems();
    protected ItemSelector exclude = ItemSelectors.trainingItems();
    protected String prefix = null;
    protected String suffix = null;
    
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
     * Get the prefix (or null if no prefix is set) to be applied to each column label
     * 
     * This property might not be supported by the built metric.
     * 
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the suffix (or null if no suffix is set) to be applied to each column label
     *
     * This property might not be supported by the built metric.
     * 
     * @return
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Set the recommendation list size.  The default size is 10.
     * @param n The recommendation list size.
     * @return The builder (for chaining).
     */
    public T setListSize(int n) {
        listSize = n;
        return (T) this;
    }

    /**
     * Set the candidate selector.  The default is {@link org.grouplens.lenskit.eval.metrics.topn.ItemSelectors#testItems()}.
     * @param sel The candidate item selector.
     * @return The builder (for chaining).
     */
    public T setCandidates(ItemSelector sel) {
        candidates = sel;
        return (T) this;
    }

    /**
     * Set the exclude item selector.  The default is {@link org.grouplens.lenskit.eval.metrics.topn.ItemSelectors#trainingItems()}.
     * @param sel The exclude item selector.
     * @return The builder (for chaining).
     */
    public T setExclude(ItemSelector sel) {
        exclude = sel;
        return (T) this;
    }

    /**
     * Set the prefix to be applied to each column label.
     * 
     * @param prefix the prefix to apply or {@code null} to set no prefix.
     * @return the builder (for chaining)
     */
    public T setPrefix(String prefix) {
        this.prefix = prefix;
        return (T) this;
    }

    /**
     * Set the suffix to be applied to each column label.
     *
     * @param suffix the suffix to apply or {@code null} to set no suffix.
     * @return the builder (for chaining)
     */
    public T setSuffix(String suffix) {
        this.suffix = suffix;
        return (T) this;

    }

    public abstract K build();
}
