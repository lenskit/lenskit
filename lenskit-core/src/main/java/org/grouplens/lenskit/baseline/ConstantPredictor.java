/*
 * LensKit, an open source recommender systems toolkit.
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
/**
 *
 */
package org.grouplens.lenskit.baseline;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;

import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.params.meta.DefaultDouble;
import org.grouplens.lenskit.params.meta.Parameter;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Rating scorer that predicts a constant rating for all items.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class ConstantPredictor implements BaselinePredictor {
    /**
     * Parameter: the value used by the constant scorer.
     */
    @Documented
    @DefaultDouble(0.0)
    @Parameter(Double.class)
    @Target({ ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Value { }

    private static final long serialVersionUID = 1L;

    private final double value;

    /**
     * Construct a new constant scorer.  This is exposed so other code
     * can use it as a fallback.
     * @param value
     */
    public ConstantPredictor(@Value double value) {
        this.value = value;
    }

    @Override
    public MutableSparseVector predict(UserVector ratings, Collection<Long> items) {
        return constantPredictions(items, value);
    }

    /**
     * Construct a rating vector with the same rating for all items.
     *
     * @param items The items to include in the vector.
     * @param value The rating/prediction to give.
     * @return A rating vector mapping all items in <var>items</var> to
     *         <var>value</var>.
     */
    public static MutableSparseVector constantPredictions(Collection<Long> items, double value) {
        long[] keys = CollectionUtils.fastCollection(items).toLongArray();
        if (!(items instanceof LongSortedSet))
            Arrays.sort(keys);
        double[] preds = new double[keys.length];
        DoubleArrays.fill(preds, value);
        return MutableSparseVector.wrap(keys, preds);
    }

    @Override
    public String toString() {
        return String.format("%s(%.3f)", getClass().getCanonicalName(), value);
    }
}
