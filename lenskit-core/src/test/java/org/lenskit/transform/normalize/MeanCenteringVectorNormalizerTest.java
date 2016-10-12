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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
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

    @Test
    public void testEmptyReferenceOld() {
        MutableSparseVector msv = MutableSparseVector.create(4L);
        msv.set(4, 3.5);
        norm.normalize(SparseVector.empty(), msv);
        assertThat(msv.get(4), closeTo(3.5, 1.0e-5));
    }

    @Test
    public void testSameReferenceOld() {
        MutableSparseVector msv = MutableSparseVector.create(4L);
        msv.set(4, 3.5);
        norm.normalize(msv, msv);
        assertThat(msv.get(4), closeTo(0, 1.0e-5));
    }

    @Test
    public void testTransformOld() {
        MutableSparseVector reference = MutableSparseVector.create(4L, 5L);
        reference.set(4, 3.5);
        reference.set(5, 2.5);
        VectorTransformation tx = norm.makeTransformation(reference);

        MutableSparseVector msv = reference.mutableCopy();
        tx.apply(msv);
        assertThat(msv.get(4), closeTo(0.5, 1.0e-5));
        assertThat(msv.get(5), closeTo(-0.5, 1.0e-5));

        msv.set(4, 2);
        tx.unapply(msv);
        assertThat(msv.get(4), closeTo(5, 1.0e-5));
    }
}
