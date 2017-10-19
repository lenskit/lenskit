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
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserItemBiasModel;
import org.lenskit.util.InvertibleFunction;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BiasUserVectorNormalizerTest {
    UserVectorNormalizer normalizer;

    @Before
    public void createNormalizer() {
        Long2DoubleMap users = new Long2DoubleOpenHashMap();
        users.put(42L, 0.5);
        users.put(37L, -0.2);
        Long2DoubleMap items = new Long2DoubleOpenHashMap();
        items.put(1L, 0.2);
        items.put(2L, -0.1);

        BiasModel model = new UserItemBiasModel(3.0, users, items);
        normalizer = new BiasUserVectorNormalizer(model);
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