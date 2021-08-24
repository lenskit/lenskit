/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.mf.BPR;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.*;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class RandomRatingPairGeneratorTest {
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
        RandomRatingPairGenerator gen = new RandomRatingPairGenerator(snapshot, new Random(), 10);
        for (int reps = 0; reps<10; reps++) {
            int i = 0;
            for (TrainingItemPair pair : gen.nextBatch()) {
                assertNotNull(pair);
                i++;
            }
            assertEquals(10, i);
        }


        gen = new RandomRatingPairGenerator(snapshot, new Random(), 1000);
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
        RandomRatingPairGenerator gen = new RandomRatingPairGenerator(snapshot, new Random(), 10000);
        for(TrainingItemPair pair : gen.nextBatch()) {
            long u = pair.u;
            long l = pair.l;
            long g = pair.g;
            assert(snapshot.getUserIds().contains(u));
            Long2DoubleMap ratingVector = snapshot.getUserRatingVector(u);
            assert(ratingVector.keySet().contains(l));
            assert(ratingVector.keySet().contains(g));
            assert(ratingVector.get(l) < ratingVector.get(g));
        }
    }

}
