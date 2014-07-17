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
/**
 *
 */
package org.grouplens.lenskit.baseline;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.io.Serializable;
import java.lang.annotation.*;
import java.util.Collection;

/**
 * Rating scorer that predicts a constant rating for all items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class ConstantItemScorer implements ItemScorer, Serializable {
    /**
     * Parameter: the value used by the constant scorer.
     */
    @Documented
    @DefaultDouble(0.0)
    @Qualifier
    @Parameter(Double.class)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Value {
    }

    private static final long serialVersionUID = 1L;

    private final double value;

    /**
     * Construct a new constant scorer.  This is exposed so other code
     * can use it as a fallback.
     *
     * @param val The value to use.
     */
    @Inject
    public ConstantItemScorer(@Value double val) {
        value = val;
    }

    @Override
    public double score(long user, long item) {
        return value;
    }

    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        return MutableSparseVector.create(items, value);
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector output) {
        output.fill(value);
    }

    @Override
    public String toString() {
        return String.format("%s(%.3f)", getClass().getCanonicalName(), value);
    }
}
