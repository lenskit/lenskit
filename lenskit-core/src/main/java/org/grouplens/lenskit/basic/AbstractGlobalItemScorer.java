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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongLists;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Base class to make global item scorers easier to implement. Delegates single=item
 * score methods to collection-based ones,
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractGlobalItemScorer implements GlobalItemScorer {
    /**
     * The DAO passed to the constructor.
     */
    @Nonnull
    protected final DataAccessObject dao;

    /**
     * Initialize the abstract item scorer.
     *
     * @param dao The data access object to use for retrieving histories.
     */
    protected AbstractGlobalItemScorer(@Nonnull DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * {@inheritDoc}
     * <p>Delegate to {@link #globalScore(Collection, Collection)}.
     */
    @Override
    public double globalScore(@Nonnull Collection<Long> queryItems, long item) {
        SparseVector v = globalScore(queryItems, LongLists.singleton(item));
        return v.get(item, Double.NaN);
    }

    @Override
    @Nonnull
    public MutableSparseVector globalScore(@Nonnull Collection<Long> queryItems,
                                           @Nonnull Collection<Long> items) {
        MutableSparseVector v = new MutableSparseVector(items);
        globalScore(queryItems, v);
        return v;
    }
}
