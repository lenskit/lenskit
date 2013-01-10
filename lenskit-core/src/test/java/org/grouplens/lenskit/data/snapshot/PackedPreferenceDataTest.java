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
package org.grouplens.lenskit.data.snapshot;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.grouplens.lenskit.data.snapshot.PackedPreferenceData.*;

import org.junit.Test;

public class PackedPreferenceDataTest {
    @Test
    public void testChunk() {
        assertThat(chunk(0), equalTo(0));
        assertThat(chunk(39), equalTo(0));
        assertThat(chunk(4095), equalTo(0));
        assertThat(chunk(4096), equalTo(1));
        assertThat(chunk(6938), equalTo(1));
        assertThat(chunk(1 << 14), equalTo(4));
    }

    @Test
    public void testElement() {
        assertThat(element(0), equalTo(0));
        assertThat(element(39), equalTo(39));
        assertThat(element(4095), equalTo(4095));
        assertThat(element(4096), equalTo(0));
        assertThat(element(6938), equalTo(6938 - 4096));
        assertThat(element(1 << 14), equalTo(0));
    }
}
