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
package org.lenskit.util;

import org.joda.convert.StringConvert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by michaelekstrand on 4/13/2017.
 */
public class TextTest {

    @Test
    public void testEmptyString() {
        Text text = Text.fromString("");
        assertThat(text.toString(), equalTo(""));
        assertThat(text.size(), equalTo(0));
    }

    @Test
    public void testBasicString() {
        Text text = Text.fromString("wumpus");
        assertThat(text.toString(), equalTo("wumpus"));
        assertThat(text.size(), equalTo(6));
    }

    @Test
    public void testStringWithLongerChar() {
        Text text = Text.fromString("wümpus");
        assertThat(text.toString(), equalTo("wümpus"));
        assertThat(text.size(), equalTo(7));
    }

    @Test
    public void testStringWithNonBMPChar() {
        Text text = Text.fromString("wu\uD835\uDCC2pus");
        assertThat(text.toString(), equalTo("wu\uD835\uDCC2pus"));
        assertThat(text.size(), equalTo(9));
    }

    @Test
    public void testJodaConvertFromString() {
        Text fb = StringConvert.INSTANCE.convertFromString(Text.class, "foobar");
        assertThat(fb.toString(), equalTo("foobar"));
    }

    @Test
    public void testJodaConvertToString() {
        Text fb = Text.fromString("foobar");
        assertThat(StringConvert.INSTANCE.convertToString(fb),
                   equalTo("foobar"));
    }
}