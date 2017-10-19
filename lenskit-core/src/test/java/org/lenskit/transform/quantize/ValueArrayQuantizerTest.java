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
package org.lenskit.transform.quantize;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ValueArrayQuantizerTest {
    @Test
    public void testFindSingle() {
        Quantizer q = new ValueArrayQuantizer(new double[]{5.0});
        assertThat(q.getCount(), equalTo(1));
        assertThat(q.getIndexValue(0), equalTo(5.0));
        assertThat(q.index(2.5), equalTo(0));
        assertThat(q.index(5.0), equalTo(0));
    }

    @Test
    public void testSomeElements() {
        Quantizer q = new ValueArrayQuantizer(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertThat(q.getCount(), equalTo(5));
        assertThat(q.getIndexValue(0), equalTo(1.0));
        assertThat(q.index(2.5), equalTo(2));
        assertThat(q.index(5.0), equalTo(4));
        assertThat(q.index(1.73), equalTo(1));
    }
}
