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
package org.lenskit.mf.bpr;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.ratings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ImplicitTrainingSamplerTest {
    private DataAccessObject dao;
    private RatingMatrix snapshot;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();

        rs.add(Rating.create(1, 1, 2));
        rs.add(Rating.create(1, 3, 1));
        rs.add(Rating.create(1, 6, 2));
        rs.add(Rating.create(1, 5, 3));
        rs.add(Rating.create(1, 8, 5));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));
        rs.add(Rating.create(8, 6, 3));
        rs.add(Rating.create(8, 9, 4));
        rs.add(Rating.create(2, 1, 2));
        rs.add(Rating.create(2, 3, 1));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(2, 5, 3));
        rs.add(Rating.create(2, 8, 5));
        rs.add(Rating.create(2, 7, 4));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(2, 51, 4));
        rs.add(Rating.create(82, 6, 3));
        rs.add(Rating.create(82, 9, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();

        RatingVectorPDAO pdao = new StandardRatingVectorPDAO(dao);
        PackedRatingMatrixProvider ratingMatrixProvider = new PackedRatingMatrixProvider(pdao, new Random());
        snapshot = ratingMatrixProvider.get();
    }

    @Test
    public void testLength() {
        ImplicitTrainingSampler gen = new ImplicitTrainingSampler(dao, CommonTypes.RATING,
                                                                  new Random(), 10);
        for (int reps = 0; reps<10; reps++) {
            int i = 0;
            for (TrainingItemPair pair : gen.nextBatch()) {
                assertNotNull(pair);
                i++;
            }
            assertEquals(10, i);
        }


        gen = new ImplicitTrainingSampler(dao, CommonTypes.RATING,
                                          new Random(), 1000);
        for (int reps = 0; reps<10; reps++) {
            int i = 0;
            for (TrainingItemPair pair : gen.nextBatch()) {
                assertNotNull(pair);
                i++;
            }
            assertEquals(1000, i);
        }
    }

    @Test
    public void testAlwaysValidTrainingPairs() {
        ImplicitTrainingSampler gen = new ImplicitTrainingSampler(dao, CommonTypes.RATING,
                                                                  new Random(), 10000);
        for(TrainingItemPair pair : gen.nextBatch()) {
            long u = pair.u;
            long g = pair.g;
            long l = pair.l;

            assertThat(u, isIn(snapshot.getUserIds()));

            Long2DoubleMap ratingVector = snapshot.getUserRatingVector(u);
            assertThat(g, isIn(ratingVector.keySet()));
            assertThat(l, not(isIn(ratingVector.keySet())));
        }
    }

}
