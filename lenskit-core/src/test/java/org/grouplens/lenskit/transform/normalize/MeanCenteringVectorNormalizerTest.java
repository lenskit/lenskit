/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.transform.normalize;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MeanCenteringVectorNormalizerTest {
    VectorNormalizer norm = new MeanCenteringVectorNormalizer();

    @Test
    public void testEmptyReference() {
        MutableSparseVector msv = MutableSparseVector.create(4L);
        msv.set(4, 3.5);
        norm.normalize(SparseVector.empty(), msv);
        assertThat(msv.get(4), closeTo(3.5, 1.0e-5));
    }

    @Test
    public void testSameReference() {
        MutableSparseVector msv = MutableSparseVector.create(4L);
        msv.set(4, 3.5);
        norm.normalize(msv, msv);
        assertThat(msv.get(4), closeTo(0, 1.0e-5));
    }

    @Test
    public void testTransform() {
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
