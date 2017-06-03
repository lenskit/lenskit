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