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
package org.lenskit.data.ratings;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.lenskit.data.ratings.PackedRatingData.*;

import org.junit.Test;

public class PackedRatingDataTest {
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
