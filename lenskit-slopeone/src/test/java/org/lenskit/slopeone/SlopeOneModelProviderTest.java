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
package org.lenskit.slopeone;

import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.data.ratings.StandardRatingVectorPDAO;
import org.lenskit.knn.item.model.ItemItemBuildContextProvider;
import org.lenskit.transform.normalize.DefaultUserVectorNormalizer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SlopeOneModelProviderTest {

    public static final double EPSILON = 1.0e-6;

    private SlopeOneModel getModel(List<Rating> ratings) {
        StaticDataSource source = StaticDataSource.fromList(ratings);
        DataAccessObject dao = source.get();
        RatingVectorPDAO rvDAO = new StandardRatingVectorPDAO(dao);
        ItemItemBuildContextProvider contextFactory = new ItemItemBuildContextProvider(
                rvDAO, new DefaultUserVectorNormalizer());
        SlopeOneModelProvider provider = new SlopeOneModelProvider(contextFactory.get(), 0);
        return provider.get();
    }

    @Test
    public void testBuild1() {

        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(2, 5, 4));
        rs.add(Rating.create(1, 3, 5));
        rs.add(Rating.create(2, 3, 4));

        SlopeOneModel model1 = getModel(rs);

        assertEquals(2, model1.getCoratings(5, 3));
        assertEquals(2, model1.getCoratings(3, 5));
        assertEquals(-1.5, model1.getDeviation(5, 3), EPSILON);
        assertEquals(1.5, model1.getDeviation(3, 5), EPSILON);
    }

    @Test
    public void testBuild2() {

        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 4, 4));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(3, 4, 4));
        rs.add(Rating.create(1, 5, 3));
        rs.add(Rating.create(2, 5, 5));
        rs.add(Rating.create(3, 5, 1));
        rs.add(Rating.create(1, 6, 1));
        rs.add(Rating.create(2, 6, 5));
        rs.add(Rating.create(3, 6, 3));

        SlopeOneModel model2 = getModel(rs);

        assertEquals(3, model2.getCoratings(4, 5));
        assertEquals(3, model2.getCoratings(5, 4));
        assertEquals(3, model2.getCoratings(4, 6));
        assertEquals(3, model2.getCoratings(6, 4));
        assertEquals(3, model2.getCoratings(5, 6));
        assertEquals(3, model2.getCoratings(6, 5));
        assertEquals(4 / 3.0, model2.getDeviation(4, 6), EPSILON);
        assertEquals(-4 / 3.0, model2.getDeviation(6, 4), EPSILON);
        assertEquals(4 / 3.0, model2.getDeviation(4, 5), EPSILON);
        assertEquals(-4 / 3.0, model2.getDeviation(5, 4), EPSILON);
        assertEquals(0, model2.getDeviation(5, 6), EPSILON);
        assertEquals(0, model2.getDeviation(6, 5), EPSILON);
    }

    @Test
    public void testBuild3() {

        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(1, 7, 3));
        rs.add(Rating.create(2, 7, 2));
        rs.add(Rating.create(3, 7, 5));
        rs.add(Rating.create(4, 7, 2));
        rs.add(Rating.create(1, 8, 3));
        rs.add(Rating.create(2, 8, 4));
        rs.add(Rating.create(3, 8, 3));
        rs.add(Rating.create(4, 8, 2));
        rs.add(Rating.create(5, 8, 3));
        rs.add(Rating.create(6, 8, 2));
        rs.add(Rating.create(1, 9, 3));
        rs.add(Rating.create(3, 9, 4));

        SlopeOneModel model3 = getModel(rs);

        assertEquals(2, model3.getCoratings(6, 7));
        assertEquals(2, model3.getCoratings(7, 6));
        assertEquals(2, model3.getCoratings(6, 8));
        assertEquals(2, model3.getCoratings(8, 6));
        assertEquals(1, model3.getCoratings(6, 9));
        assertEquals(1, model3.getCoratings(9, 6));
        assertEquals(4, model3.getCoratings(7, 8));
        assertEquals(4, model3.getCoratings(8, 7));
        assertEquals(2, model3.getCoratings(7, 9));
        assertEquals(2, model3.getCoratings(9, 7));
        assertEquals(2, model3.getCoratings(8, 9));
        assertEquals(2, model3.getCoratings(9, 8));
        assertEquals(0.5, model3.getDeviation(6, 7), EPSILON);
        assertEquals(-0.5, model3.getDeviation(7, 6), EPSILON);
        assertEquals(-0.5, model3.getDeviation(6, 8), EPSILON);
        assertEquals(0.5, model3.getDeviation(8, 6), EPSILON);
        assertEquals(1, model3.getDeviation(6, 9), EPSILON);
        assertEquals(-1, model3.getDeviation(9, 6), EPSILON);
        assertEquals(0, model3.getDeviation(7, 8), EPSILON);
        assertEquals(0, model3.getDeviation(8, 7), EPSILON);
        assertEquals(0.5, model3.getDeviation(7, 9), EPSILON);
        assertEquals(-0.5, model3.getDeviation(9, 7), EPSILON);
        assertEquals(-0.5, model3.getDeviation(8, 9), EPSILON);
        assertEquals(0.5, model3.getDeviation(9, 8), EPSILON);
    }

    @Test
    public void testBuild4() {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 4, 3.5));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(3, 5, 4.25));
        rs.add(Rating.create(2, 6, 3));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(2, 7, 4));
        rs.add(Rating.create(3, 7, 1.5));

        SlopeOneModel model4 = getModel(rs);

        assertEquals(0, model4.getCoratings(4, 5));
        assertEquals(0, model4.getCoratings(5, 4));
        assertEquals(1, model4.getCoratings(4, 6));
        assertEquals(1, model4.getCoratings(6, 4));
        assertEquals(2, model4.getCoratings(4, 7));
        assertEquals(2, model4.getCoratings(7, 4));
        assertEquals(0, model4.getCoratings(5, 6));
        assertEquals(0, model4.getCoratings(6, 5));
        assertEquals(1, model4.getCoratings(5, 7));
        assertEquals(1, model4.getCoratings(7, 5));
        assertEquals(1, model4.getCoratings(6, 7));
        assertEquals(1, model4.getCoratings(7, 6));
        assertEquals(Double.NaN, model4.getDeviation(4, 5), 0);
        assertEquals(Double.NaN, model4.getDeviation(5, 4), 0);
        assertEquals(2, model4.getDeviation(4, 6), EPSILON);
        assertEquals(-2, model4.getDeviation(6, 4), EPSILON);
        assertEquals(0.25, model4.getDeviation(4, 7), EPSILON);
        assertEquals(-0.25, model4.getDeviation(7, 4), EPSILON);
        assertEquals(Double.NaN, model4.getDeviation(5, 6), 0);
        assertEquals(Double.NaN, model4.getDeviation(6, 5), 0);
        assertEquals(2.75, model4.getDeviation(5, 7), EPSILON);
        assertEquals(-2.75, model4.getDeviation(7, 5), EPSILON);
        assertEquals(-1, model4.getDeviation(6, 7), EPSILON);
        assertEquals(1, model4.getDeviation(7, 6), EPSILON);
    }
}
