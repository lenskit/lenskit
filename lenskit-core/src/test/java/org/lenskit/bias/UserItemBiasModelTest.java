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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import net.java.quickcheck.Generator;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import java.util.Map;
import java.util.Set;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someMaps;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSets;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static net.java.quickcheck.generator.PrimitiveGenerators.positiveLongs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserItemBiasModelTest {
    @Test
    public void testZeroBias() {
        BiasModel model = new UserItemBiasModel(0, Long2DoubleMaps.EMPTY_MAP, Long2DoubleMaps.EMPTY_MAP);
        assertThat(model.getIntercept(), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testBasicBias() {
        BiasModel model = new UserItemBiasModel(Math.PI, Long2DoubleMaps.EMPTY_MAP, Long2DoubleMaps.EMPTY_MAP);
        assertThat(model.getIntercept(), equalTo(Math.PI));
        assertThat(model.getUserBias(42L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testWithItems() {
        BiasModel model = new UserItemBiasModel(1.5, Long2DoubleMaps.EMPTY_MAP,
                                                Long2DoubleMaps.singleton(42L, 1.0));
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getItemBias(42L), equalTo(1.0));
        assertThat(model.getItemBias(37L), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
    }

    @Test
    public void testWithUsers() {
        BiasModel model = new UserItemBiasModel(1.5, Long2DoubleMaps.singleton(42L, 1.0), Long2DoubleMaps.EMPTY_MAP);
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getUserBias(42L), equalTo(1.0));
        assertThat(model.getUserBias(37L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testManyUsers() {
        Generator<Double> globals = doubles();
        for (Map<Long,Double> map: someMaps(positiveLongs(), doubles())) {
            double bias = globals.next();
            Long2DoubleMap userBiases = Long2DoubleSortedArrayMap.create(map);
            BiasModel model = new UserItemBiasModel(bias, userBiases, Long2DoubleMaps.EMPTY_MAP);
            assertThat(model.getIntercept(), equalTo(bias));
            assertThat(model.getUserBiases(userBiases.keySet()),
                       equalTo(userBiases));

            for (Set<Long> users : someSets(positiveLongs())) {
                Long2DoubleMap biases = model.getUserBiases(LongUtils.packedSet(users));
                for (long user: users) {
                    if (userBiases.containsKey(user)) {
                        assertThat(biases.get(user), equalTo(userBiases.get(user)));
                    } else {
                        assertThat(biases.get(user), equalTo(0.0));
                    }
                }
            }
        }
    }

    @Test
    public void testManyItems() {
        Generator<Double> globals = doubles();
        for (Map<Long,Double> map: someMaps(positiveLongs(), doubles())) {
            double bias = globals.next();
            Long2DoubleMap itemBiases = Long2DoubleSortedArrayMap.create(map);
            BiasModel model = new UserItemBiasModel(bias, Long2DoubleMaps.EMPTY_MAP, itemBiases);
            assertThat(model.getIntercept(), equalTo(bias));
            assertThat(model.getItemBiases(itemBiases.keySet()),
                       equalTo(itemBiases));

            for (Set<Long> users : someSets(positiveLongs())) {
                Long2DoubleMap biases = model.getItemBiases(LongUtils.packedSet(users));
                for (long user: users) {
                    if (itemBiases.containsKey(user)) {
                        assertThat(biases.get(user), equalTo(itemBiases.get(user)));
                    } else {
                        assertThat(biases.get(user), equalTo(0.0));
                    }
                }
            }
        }
    }

    @Test
    public void testComputeItemMeans() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(101, 201, 2.5),
                               efac.rating(102, 203, 4.5),
                               efac.rating(103, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).toProvider(ItemAverageRatingBiasModelProvider.class);

        LenskitRecommender rec = LenskitRecommender.build(config, daoBuilder.build());
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getItemBias(200), closeTo(0.0, 1.0e-3));
        assertThat(model.getItemBias(201), closeTo(-1.0, 1.0e-3));
        assertThat(model.getItemBias(203), closeTo(0.5, 1.0e-3));
    }

    @Test
    public void testComputeUserMeans() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(102, 201, 2.5),
                               efac.rating(102, 203, 4.5),
                               efac.rating(101, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).toProvider(UserAverageRatingBiasModelProvider.class);

        LenskitRecommender rec = LenskitRecommender.build(config, daoBuilder.build());
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getUserBias(100), closeTo(-0.5, 1.0e-3));
        assertThat(model.getUserBias(101), closeTo(0.25, 1.0e-3));
        assertThat(model.getUserBias(102), closeTo(0.0, 1.0e-3));
    }

    @Test
    public void testComputeAllMeans() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(102, 201, 2.5),
                               efac.rating(102, 203, 4.5),
                               efac.rating(101, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).toProvider(UserItemAverageRatingBiasModelProvider.class);

        LenskitRecommender rec = LenskitRecommender.build(config, daoBuilder.build());
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getItemBias(200), closeTo(0.0, 1.0e-3));
        assertThat(model.getItemBias(201), closeTo(-1.0, 1.0e-3));
        assertThat(model.getItemBias(203), closeTo(0.5, 1.0e-3));
        assertThat(model.getUserBias(100), closeTo(-0.5, 1.0e-3));
        assertThat(model.getUserBias(101), closeTo(0, 1.0e-3));
        assertThat(model.getUserBias(102), closeTo(0.25, 1.0e-3));
    }
}