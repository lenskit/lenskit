/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MeanVarianceNormalizerTest {
    private final static double MIN_DOUBLE_PRECISION = 0.00001;

    private DataAccessObject dao;
    private Long2DoubleMap userRatings;
    private Long2DoubleMap uniformUserRatings;

    private void addRating(List<Rating> ratings, long uid, long iid, double value) {
        ratings.add(Rating.create(uid, iid, value));
    }

    @Before
    public void setUp() {

        long[] keys = {0L, 1L, 2L};
        double[] values = {0., 2., 4.};
        userRatings = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
        double[] uniformValues = {2., 2., 2.};
        uniformUserRatings = Long2DoubleSortedArrayMap.wrapUnsorted(keys, uniformValues);
        List<Rating> ratings = new ArrayList<>();
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
        dao = StaticDataSource.fromList(ratings).get();
    }

    @Test
    public void testMakeTransformation() {
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(userRatings);
        Long2DoubleMap nUR = trans.apply(userRatings);
        final double mean = 2.0;
        final double stdev = Math.sqrt(8.0 / 3.0);

        assertThat(nUR, notNullValue());

        //Test apply
        Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);

        //Test unapply
        nUR = trans.unapply(nUR);
        Assert.assertEquals(0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
    }

    @Test
    public void testUniformRatings() {
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(uniformUserRatings);
        Long2DoubleMap nUR = trans.apply(userRatings);
        assertThat(nUR, notNullValue());

        //Test apply - shoudl subtract mean
        assertThat(nUR.get(0L), closeTo(-2.0, 1.0e-6));
        assertThat(nUR.get(1L), closeTo(0.0, 1.0e-6));
        assertThat(nUR.get(2L), closeTo(2.0, 1.0e-6));
        nUR = trans.unapply(nUR);

        //Test unapply
        assertThat(nUR.get(0L), closeTo(0.0, 1.0e-6));
        assertThat(nUR.get(1L), closeTo(2.0, 1.0e-6));
        assertThat(nUR.get(2L), closeTo(4.0, 1.0e-6));
    }

    @Test
    @Ignore("removed builder")
    public void testSmoothingDetailed() {
        MeanVarianceNormalizer urvn = null; //new MeanVarianceNormalizer.Builder(dao, 3.0).get();

        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(userRatings);
        final double mean = 2.0;
        final double stdev = Math.sqrt(7.0 / 3.0);
        Long2DoubleMap nUR = trans.apply(userRatings);
        assertThat(nUR, notNullValue());

        //Test apply
        Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);

        //Test unapply
        nUR = trans.unapply(nUR);
        Assert.assertEquals(0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
    }

    @Test
    public void testMakeTransformationVector() {
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(userRatings);
        Long2DoubleMap nUR = trans.apply(userRatings);
        final double mean = 2.0;
        final double stdev = Math.sqrt(8.0 / 3.0);
        //Test apply
        Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);

        //Test unapply
        nUR = trans.unapply(nUR);
        Assert.assertEquals(0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
    }

    @Test
    public void testUniformRatingsVector() {
        MeanVarianceNormalizer urvn;
        urvn = new MeanVarianceNormalizer();
        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(uniformUserRatings);
        Long2DoubleMap nUR = trans.apply(userRatings);
        //Test apply - shoudl subtract mean
        assertThat(nUR.get(0L), closeTo(-2.0, 1.0e-6));
        assertThat(nUR.get(1L), closeTo(0.0, 1.0e-6));
        assertThat(nUR.get(2L), closeTo(2.0, 1.0e-6));

        //Test unapply
        nUR = trans.unapply(nUR);
        assertThat(nUR.get(0L), closeTo(0.0, 1.0e-6));
        assertThat(nUR.get(1L), closeTo(2.0, 1.0e-6));
        assertThat(nUR.get(2L), closeTo(4.0, 1.0e-6));
    }

    @Test
    @Ignore("removed smoothing")
    public void testSmoothingDetailedOldVector() {
        MeanVarianceNormalizer urvn = null; // new MeanVarianceNormalizer.Builder(dao, 3.0).get();

        InvertibleFunction<Long2DoubleMap,Long2DoubleMap> trans = urvn.makeTransformation(userRatings);
        Long2DoubleOpenHashMap nUR = new Long2DoubleOpenHashMap(userRatings);
        final double mean = 2.0;
        final double stdev = Math.sqrt(7.0 / 3.0);
        trans.apply(nUR);
        //Test apply
        Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);
        trans.unapply(nUR);
        //Test unapply
        Assert.assertEquals(0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
        Assert.assertEquals(4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
    }
}
