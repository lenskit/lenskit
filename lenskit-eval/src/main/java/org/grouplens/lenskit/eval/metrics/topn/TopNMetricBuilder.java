/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class TopNMetricBuilder<K> implements Builder<K> {
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
     * Get the prefix (or null if no prefix is set) to be applied to each column label.
     * If not null, this prefix will be added to the beginning of each column label, separated by a '.'
     * 
     * This property might not be supported by the built metric.
     * 
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the suffix (or null if no suffix is set) to be applied to each column label.
     * If not null, this suffix will be added to the end of each column label, separated by a '.'
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
     */
    public void setListSize(int n) {
        listSize = n;
    }

    /**
     * Set the candidate selector.  The default is {@link org.grouplens.lenskit.eval.metrics.topn.ItemSelectors#testItems()}.
     * @param sel The candidate item selector.
     */
    public void setCandidates(ItemSelector sel) {
        candidates = sel;
    }

    /**
     * Set the exclude item selector.  The default is {@link org.grouplens.lenskit.eval.metrics.topn.ItemSelectors#trainingItems()}.
     * @param sel The exclude item selector.
     */
    public void setExclude(ItemSelector sel) {
        exclude = sel;
    }

    /**
     * Set the prefix to be applied to each column label.
     * If not null, the prefix will be added to the beginning of each column label, separated by a '.'
     * 
     * @param prefix the prefix to apply or {@code null} to set no prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Set the suffix to be applied to each column label.
     * If not null, the suffix will be added to the end of each column label, separated by a '.'
     *
     * @param suffix the suffix to apply or {@code null} to set no suffix.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;

    }

    public abstract K build();
}
