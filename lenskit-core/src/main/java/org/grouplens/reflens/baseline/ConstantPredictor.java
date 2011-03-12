/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.vector.MutableSparseVector;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.params.meta.DefaultDouble;
import org.grouplens.reflens.params.meta.Parameter;
import org.grouplens.reflens.util.CollectionUtils;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

/**
 * Rating predictor that predicts a constant rating for all items.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ConstantPredictor implements RatingPredictor {
	private final double value;
	
	/**
	 * Annotation for value parameters to the recommender.
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	@BindingAnnotation
	@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Parameter
	@DefaultDouble(0)
	public static @interface Value {
	}
	
	/**
	 * Construct a rating vector with the same rating for all items.
	 * @param items The items to include in the vector.
	 * @param value The rating/prediction to give.
	 * @return A rating vector mapping all items in <var>items</var> to
	 * <var>value</var>.
	 */
	public static MutableSparseVector constantPredictions(Collection<Long> items, double value) {
		long[] keys = CollectionUtils.fastCollection(items).toLongArray();
		if (!(items instanceof LongSortedSet))
			Arrays.sort(keys);
		double[] preds = new double[keys.length];
		Arrays.fill(preds, value);
		return MutableSparseVector.wrap(keys, preds);
	}
	
	/**
	 * Construct a new constant predictor.  This is exposed so other code
	 * can use it as a fallback.
	 * @param value
	 */
	@Inject
	public ConstantPredictor(@Value double value) {
		this.value = value;
	}
	
	@Override
	public MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
		return constantPredictions(items, value);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(java.lang.Object, java.util.Map, java.lang.Object)
	 */
	@Override
	public ScoredId predict(long user, SparseVector profile, long item) {
		return new ScoredId(item, value);
	}
}
