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
package org.grouplens.lenskit.vectors;

import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ScoredIdConversionTest {
    ScoredIdListBuilder builder;

    @Before
    public void createBuilder() {
        builder = ScoredIds.newListBuilder();
    }

    @Test
    public void testEmpty() {
        MutableSparseVector vec = Vectors.fromScoredIds(Collections.<ScoredId>emptyList());
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testSingleton() {
        builder.add(1, 3.5);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(1));
        assertThat(vec.get(1), equalTo(3.5));
    }

    @Test
    public void testSome() {
        builder.add(1, 3.5);
        builder.add(3, 5.2);
        builder.add(-1, 0.2);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(3));
        assertThat(vec.get(1), equalTo(3.5));
        assertThat(vec.get(3), equalTo(5.2));
        assertThat(vec.get(-1), equalTo(0.2));
    }

    @Test
    public void testDuplicate() {
        builder.add(1, 3.5);
        builder.add(3, 5.2);
        builder.add(-1, 0.2);
        builder.add(3, 0.8);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(3));
        assertThat(vec.get(1), equalTo(3.5));
        assertThat(vec.get(3), equalTo(5.2));
        assertThat(vec.get(-1), equalTo(0.2));
    }
}
