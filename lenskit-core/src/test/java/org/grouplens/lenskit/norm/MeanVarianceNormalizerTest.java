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
package org.grouplens.lenskit.norm;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 *
 */
public class MeanVarianceNormalizerTest {
    private final static double MIN_DOUBLE_PRECISION = 0.00001;
    private int eid = 0;

    private DataAccessObject dao;
    private ImmutableSparseVector userRatings;
    private ImmutableSparseVector uniformUserRatings;

    private void addRating(List<Rating> ratings, long uid, long iid, double value) {
        ratings.add(new SimpleRating(eid++, uid, iid, value));
    }

    @Before
    public void setUp() {

        long[] keys = {0L, 1L, 2L};
        double[] values = {0., 2., 4.};
        userRatings = MutableSparseVector.wrap(keys, values).freeze();
        double[] uniformValues = {2., 2., 2.};
        uniformUserRatings = MutableSparseVector.wrap(keys, uniformValues).freeze();
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
        dao = new EventCollectionDAO.Factory(ratings).create();
    }

    @After
    public void close() {
        dao.close();
    }

    @Test
    public void testBuilderNoSmoothing() {
        MeanVarianceNormalizer urvn = new MeanVarianceNormalizer.Provider(dao, 0).get();
        Assert.assertEquals(0.0, urvn.getGlobalVariance(), 0.0);
    }

    @Test
    public void testBuilderSmoothing() {
        MeanVarianceNormalizer urvn = new MeanVarianceNormalizer.Provider(dao, 3).get();
        Assert.assertEquals(3.0, urvn.getSmoothing(), 0.0);
        Assert.assertEquals(2.0, urvn.getGlobalVariance(), MIN_DOUBLE_PRECISION);
    }

    @Test
    public void testMakeTransformation() {
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        VectorTransformation trans = urvn.makeTransformation(userRatings);
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
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        VectorTransformation trans = urvn.makeTransformation(uniformUserRatings);
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
        MeanVarianceNormalizer urvn = new MeanVarianceNormalizer.Provider(dao, 3.0).get();

        VectorTransformation trans = urvn.makeTransformation(userRatings);
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
