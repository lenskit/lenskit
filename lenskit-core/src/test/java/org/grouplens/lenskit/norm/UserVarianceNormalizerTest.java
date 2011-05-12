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
package org.grouplens.lenskit.norm;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.PackedRatingSnapshot;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 *
 */
public class UserVarianceNormalizerTest {
	RatingDataAccessObject dao;
	RatingBuildContext rs;
	SparseVector userRatings;
	SparseVector uniformUserRatings;
	UserVarianceNormalizer.Builder builder;
	final static double MIN_DOUBLE_PRECISION = 0.00001;
	
	private static void addRating(List<Rating> ratings, long uid, long iid, double value) {
	    ratings.add(new SimpleRating(uid, iid, value));
	}
	
	@Before
	public void setUp() {
	    builder = new UserVarianceNormalizer.Builder();
	    
		long[] keys = {0L, 1L, 2L};
		double[] values = {0., 2., 4.};
		userRatings = SparseVector.wrap(keys, values);
		double[] uniformValues = {2., 2., 2.};
		uniformUserRatings = SparseVector.wrap(keys, uniformValues);
		List<Rating> ratings = new ArrayList<Rating>();
		addRating(ratings, 0, 0, 0);
		addRating(ratings, 0, 1, 1);
		addRating(ratings, 0, 2, 2);
		addRating(ratings, 0, 3, 3);
		addRating(ratings, 0, 4, 4);
		addRating(ratings, 0, 5, 5);
		addRating(ratings, 0, 6, 6);
		addRating(ratings, 1, 0, 3);
		addRating(ratings, 1, 1, 3);
		addRating(ratings, 1, 2, 3);
		addRating(ratings, 1, 3, 3);
		addRating(ratings, 1, 4, 3);
		addRating(ratings, 1, 5, 3);
		addRating(ratings, 1, 6, 3);
		dao = new RatingCollectionDAO.Manager(ratings).open();
		PackedRatingSnapshot rs = new PackedRatingSnapshot.Builder(dao).build();
		this.rs = new PackedRatingBuildContext(dao, rs);
		builder.setRatingBuildContext(this.rs);
	}
	
	@After
	public void close() {
	    rs.close();
	    dao.close();
	}

	@Test
	public void testBuilderNoSmoothing() {
		UserVarianceNormalizer urvn = builder.build();
		Assert.assertEquals(0.0, urvn.getGlobalVariance(), 0.0);
	}
	
	@Test
	public void testBuilderSmoothing() {
	    builder.setSmoothing(3);
        UserVarianceNormalizer urvn = builder.build();
        Assert.assertEquals(3.0, urvn.getSmoothing(), 0.0);
        Assert.assertEquals(2.0, urvn.getGlobalVariance(), MIN_DOUBLE_PRECISION);
	}

	@Test
	public void testMakeTransformation() {
		UserVarianceNormalizer urvn;
		urvn = new UserVarianceNormalizer();
		VectorTransformation trans = urvn.makeTransformation(9001, userRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		final double mean = 2.0;
		final double stdev = Math.sqrt(8.0 / 3.0);
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}

	@Test
	public void testUniformRatings() {
		UserVarianceNormalizer urvn;
		urvn = new UserVarianceNormalizer();
		VectorTransformation trans = urvn.makeTransformation(9001, uniformUserRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 0.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 0.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 2.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}	
	
	@Test
	public void testSmoothingDetailed() {
	    builder.setSmoothing(3.0);
		UserVarianceNormalizer urvn = builder.build();

		VectorTransformation trans = urvn.makeTransformation(9001, userRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		final double mean = 2.0;
		final double stdev = Math.sqrt(7.0/3.0);
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}

}
