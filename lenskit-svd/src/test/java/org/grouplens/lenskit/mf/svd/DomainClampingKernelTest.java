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
package org.grouplens.lenskit.mf.svd;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.ImmutableVec;
import org.grouplens.lenskit.vectors.MutableVec;
import org.grouplens.lenskit.vectors.Vec;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DomainClampingKernelTest {
    BiasedMFKernel kernel = new DomainClampingKernel(PreferenceDomain.fromString("[1,5]"));

    @Test
    public void testEmptyVectors() throws Exception {
        assertThat(kernel.apply(Math.PI, MutableVec.create(0), MutableVec.create(0)),
                   equalTo(Math.PI));
    }

    @Test
    public void testBasicVectors() throws Exception {
        Vec uv = ImmutableVec.create(0.1, 0.2);
        Vec iv = ImmutableVec.create(0.1, -0.5);
        assertThat(kernel.apply(Math.PI, uv, iv),
                   closeTo(Math.PI + 0.01 - 0.1, 1.0e-5));
    }

    @Test
    public void testClamping() throws Exception {
        Vec uv = ImmutableVec.create(2, 0.2);
        Vec iv = ImmutableVec.create(2, -0.5);
        assertThat(kernel.apply(3, uv, iv),
                   closeTo(4.9, 1.0e-5));
    }
}
