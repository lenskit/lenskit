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
import org.junit.Test;
import org.lenskit.util.InvertibleFunction;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MeanCenteringVectorNormalizerTest {
    VectorNormalizer norm = new MeanCenteringVectorNormalizer();

    @Test
    public void testTransform() {
        Long2DoubleMap reference = new Long2DoubleOpenHashMap();
        reference.put(4L, 3.5);
        reference.put(5L, 2.5);

        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> tx = norm.makeTransformation(reference);

        Long2DoubleMap out = tx.apply(reference);
        assertThat(out, notNullValue());
        assertThat(out.get(4L), closeTo(0.5, 1.0e-5));
        assertThat(out.get(5L), closeTo(-0.5, 1.0e-5));

        Long2DoubleMap toRev = new Long2DoubleOpenHashMap(out);
        toRev.put(4L, 2.0);
        out = tx.unapply(toRev);
        assertThat(out.get(4L), closeTo(5, 1.0e-5));
    }
}
