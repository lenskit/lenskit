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
package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.knn.item.model.ItemItemBuildContextFactory;
import org.grouplens.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSlopeOneModelBuilder {

    public static final double EPSILON = 1.0e-6;

    private SlopeOneModel getModel(DAOFactory factory) {
        DataAccessObject dao = factory.create();
        UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        ItemItemBuildContextFactory contextFactory = new ItemItemBuildContextFactory(
                dao, new DefaultUserVectorNormalizer(), summarizer);
        SlopeOneModelProvider provider = new SlopeOneModelProvider(
                dao, null, contextFactory, 0);
        return provider.get();
    }

    @Test
    public void testBuild1() {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(2, 5, 4));
        rs.add(Ratings.make(1, 3, 5));
        rs.add(Ratings.make(2, 3, 4));

        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        SlopeOneModel model1 = getModel(manager);

        assertEquals(2, model1.getCoratings(5, 3));
        assertEquals(2, model1.getCoratings(3, 5));
        assertEquals(-1.5, model1.getDeviation(5, 3), EPSILON);
        assertEquals(1.5, model1.getDeviation(3, 5), EPSILON);
    }

    @Test
    public void testBuild2() {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 4, 4));
        rs.add(Ratings.make(2, 4, 5));
        rs.add(Ratings.make(3, 4, 4));
        rs.add(Ratings.make(1, 5, 3));
        rs.add(Ratings.make(2, 5, 5));
        rs.add(Ratings.make(3, 5, 1));
        rs.add(Ratings.make(1, 6, 1));
        rs.add(Ratings.make(2, 6, 5));
        rs.add(Ratings.make(3, 6, 3));

        EventCollectionDAO.Factory factory = new EventCollectionDAO.Factory(rs);
        SlopeOneModel model2 = getModel(factory);

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

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 6, 4));
        rs.add(Ratings.make(2, 6, 2));
        rs.add(Ratings.make(1, 7, 3));
        rs.add(Ratings.make(2, 7, 2));
        rs.add(Ratings.make(3, 7, 5));
        rs.add(Ratings.make(4, 7, 2));
        rs.add(Ratings.make(1, 8, 3));
        rs.add(Ratings.make(2, 8, 4));
        rs.add(Ratings.make(3, 8, 3));
        rs.add(Ratings.make(4, 8, 2));
        rs.add(Ratings.make(5, 8, 3));
        rs.add(Ratings.make(6, 8, 2));
        rs.add(Ratings.make(1, 9, 3));
        rs.add(Ratings.make(3, 9, 4));

        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        SlopeOneModel model3 = getModel(manager);

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
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 4, 3.5));
        rs.add(Ratings.make(2, 4, 5));
        rs.add(Ratings.make(3, 5, 4.25));
        rs.add(Ratings.make(2, 6, 3));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(2, 7, 4));
        rs.add(Ratings.make(3, 7, 1.5));

        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        SlopeOneModel model4 = getModel(manager);

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
