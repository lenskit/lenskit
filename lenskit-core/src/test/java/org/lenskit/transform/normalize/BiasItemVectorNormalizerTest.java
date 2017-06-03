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
import org.junit.Before;
import org.junit.Test;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserItemBiasModel;
import org.lenskit.util.InvertibleFunction;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BiasItemVectorNormalizerTest {
    ItemVectorNormalizer normalizer;

    @Before
    public void createNormalizer() {
        Long2DoubleMap items = new Long2DoubleOpenHashMap();
        items.put(42L, 0.5);
        items.put(37L, -0.2);
        Long2DoubleMap users = new Long2DoubleOpenHashMap();
        users.put(1L, 0.2);
        users.put(2L, -0.1);

        BiasModel model = new UserItemBiasModel(3.0, users, items);
        normalizer = new BiasItemVectorNormalizer(model);
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
}