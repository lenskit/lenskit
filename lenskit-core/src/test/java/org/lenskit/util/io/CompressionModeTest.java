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
package org.lenskit.util.io;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CompressionModeTest {
    @Test
    public void testNoneNone() {
        assertThat(CompressionMode.NONE.getEffectiveCompressionMode("foo.txt"),
                   equalTo(CompressionMode.NONE));
        assertThat(CompressionMode.NONE.getEffectiveCompressionMode("foo.gz"),
                   equalTo(CompressionMode.NONE));
    }

    @Test
    public void testAutoGZ() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.gz"),
                   equalTo(CompressionMode.GZIP));
    }

    @Test
    public void testAutoXZ() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.xz"),
                   equalTo(CompressionMode.XZ));
    }

    @Test
    public void testAutoNone() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.txt"),
                   equalTo(CompressionMode.NONE));
    }
}
