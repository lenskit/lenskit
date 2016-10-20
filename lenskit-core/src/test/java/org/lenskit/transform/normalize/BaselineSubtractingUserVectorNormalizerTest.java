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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserItemBiasModel;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.*;
import org.lenskit.util.InvertibleFunction;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class BaselineSubtractingUserVectorNormalizerTest {
    LenskitRecommender recommender;
    UserVectorNormalizer normalizer;

    @Before
    public void createNormalizer() {
        EntityFactory ef = new EntityFactory();
        EntityCollectionDAOBuilder db = new EntityCollectionDAOBuilder();

        /* Set up so that b=3.0, b(u=42) = 0.5, b(u=37) = -0.2, b(i=1) = 0.2, b(i=2) = -0.1 */
        db.addEntities(ef.rating(42, 7, 3.0),
                       ef.rating(42, 8, 4.0),
                       ef.rating(37, 9, 3.0),
                       ef.rating(37, 10, 2.6),
                       ef.rating(99, 1, 3.2),
                       ef.rating(99, 2, 2.9),
                       ef.rating(99, 7, 2),
                       ef.rating(99, 8, 3),
                       ef.rating(99, 9, 3.4),
                       ef.rating(99, 10, 3.0),
                       ef.rating(99, 99, 2.9));
        db.deriveEntities(CommonTypes.ITEM,
                          CommonTypes.RATING,
                          CommonAttributes.ITEM_ID);
        db.deriveEntities(CommonTypes.USER,
                          CommonTypes.RATING,
                          CommonAttributes.USER_ID);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(BaselineScorer.class, ItemScorer.class).to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        config.bind(UserVectorNormalizer.class).to(BaselineSubtractingUserVectorNormalizer.class);
        config.addRoot(UserVectorNormalizer.class);

        recommender = LenskitRecommender.build(config, db.build());

        normalizer = recommender.get(UserVectorNormalizer.class);
    }

    @After
    public void destroyRecommender() {
        recommender.close();
    }

    @Test
    public void testNormalizeVectorForUser() {
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> tx =
                normalizer.makeTransformation(42L, Long2DoubleMaps.EMPTY_MAP);

        Long2DoubleMap vec = new Long2DoubleOpenHashMap();
        vec.put(1L, 3.0);
        vec.put(2L, 3.5);
        vec.put(3L, 4.0);

        Long2DoubleMap out = tx.apply(vec);
        assertThat(out.get(1L), closeTo(3.0 - 3.0 - 0.5 - 0.2, 0.0001));
        assertThat(out.get(2L), closeTo(3.5 - 3.0 - 0.5 + 0.1, 0.0001));
        assertThat(out.get(3L), closeTo(4.0 - 3.0 - 0.5, 0.0001));
    }

    @Test
    public void testDenormalizeVectorForUser() {
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> tx =
                normalizer.makeTransformation(42L, Long2DoubleMaps.EMPTY_MAP);

        Long2DoubleMap vec = new Long2DoubleOpenHashMap();
        vec.put(1L, -1.0);
        vec.put(2L, -0.5);
        vec.put(3L, 0.2);

        Long2DoubleMap out = tx.unapply(vec);
        assertThat(out.get(1L), closeTo(-1.0 + 3.0 + 0.5 + 0.2, 0.0001));
        assertThat(out.get(2L), closeTo(-0.5 + 3.0 + 0.5 - 0.1, 0.0001));
        assertThat(out.get(3L), closeTo(0.2 + 3.0 + 0.5, 0.0001));
    }

    @Test
    public void testNormalizeOldVectorForUser() {
        VectorTransformation tx = normalizer.makeTransformation(42L, SparseVector.empty());

        MutableSparseVector vec = MutableSparseVector.create(1L, 2L, 3L);
        vec.set(1L, 3.0);
        vec.set(2L, 3.5);
        vec.set(3L, 4.0);
        assertThat(tx.apply(vec), sameInstance(vec));
        assertThat(vec.get(1L), closeTo(3.0 - 3.0 - 0.5 - 0.2, 0.0001));
        assertThat(vec.get(2L), closeTo(3.5 - 3.0 - 0.5 + 0.1, 0.0001));
        assertThat(vec.get(3L), closeTo(4.0 - 3.0 - 0.5, 0.0001));
    }

    @Test
    public void testDenormalizeOldVectorForUser() {
        VectorTransformation tx = normalizer.makeTransformation(42L, SparseVector.empty());

        MutableSparseVector vec = MutableSparseVector.create(1L, 2L, 3L);
        vec.set(1L, -1.0);
        vec.set(2L, -0.5);
        vec.set(3L, 0.2);
        assertThat(tx.unapply(vec), sameInstance(vec));
        assertThat(vec.get(1L), closeTo(-1.0 + 3.0 + 0.5 + 0.2, 0.0001));
        assertThat(vec.get(2L), closeTo(-0.5 + 3.0 + 0.5 - 0.1, 0.0001));
        assertThat(vec.get(3L), closeTo(0.2 + 3.0 + 0.5, 0.0001));
    }

    @Test
    public void testNormalizeValueForUser() {
        VectorTransformation tx = normalizer.makeTransformation(42L, SparseVector.empty());
        assertThat(tx.apply(1L, 3.7), closeTo(0, 0.0001));
        assertThat(tx.apply(3L, 3.7), closeTo(0.2, 0.0001));
    }

    @Test
    public void testDenormalizeValueForUser() {
        VectorTransformation tx = normalizer.makeTransformation(37L, SparseVector.empty());
        assertThat(tx.unapply(1L, 0), closeTo(3.0, 0.0001));
        assertThat(tx.unapply(3L, 0), closeTo(2.8, 0.0001));
    }

    @Test
    public void testValueForUnknownUser() {
        VectorTransformation tx = normalizer.makeTransformation(92L, SparseVector.empty());
        assertThat(tx.apply(1L, 4), closeTo(0.8, 0.0001));
        assertThat(tx.unapply(3L, 1.2), closeTo(4.2, 0.0001));
    }
}