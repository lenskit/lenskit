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

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainBuilderTest {
    @Test
    public void testEmptyBuilder() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        try {
            bld.build();
            fail("builder should throw an exception with no arguments");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testParamBuilder() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder(1, 5);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(1.0));
        assertThat(dom.getMaximum(), equalTo(5.0));
        assertThat(dom.getPrecision(), equalTo(0.0));
    }

    @Test
    public void testSetMinMax() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        bld.setMinimum(-1)
           .setMaximum(1);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(-1.0));
        assertThat(dom.getMaximum(), equalTo(1.0));
        assertThat(dom.getPrecision(), equalTo(0.0));
    }

    @Test
    public void testSetAll() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        bld.setMinimum(1.0)
           .setMaximum(5)
           .setPrecision(0.5);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(1.0));
        assertThat(dom.getMaximum(), equalTo(5.0));
        assertThat(dom.getPrecision(), equalTo(0.5));
    }

}
