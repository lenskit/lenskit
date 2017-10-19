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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RatingBuilderTest {
    @Test
    public void testInitialState() {
        RatingBuilder rb = new RatingBuilder();
        try {
            rb.build();
            fail("building a rating should fail");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testSetUserId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(42);
        assertThat(rb.getUserId(), equalTo(42L));
    }

    @Test
    public void testSetItemId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setItemId(42);
        assertThat(rb.getItemId(), equalTo(42L));
    }

    @Test
    public void testSetRating() {
        RatingBuilder rb = new RatingBuilder();
        rb.setRating(3.5);
        assertThat(rb.getRating(), equalTo(3.5));
    }

    @Test
    public void testSetTimestamp() {
        RatingBuilder rb = new RatingBuilder();
        rb.setTimestamp(235909);
        assertThat(rb.getTimestamp(), equalTo(235909L));
    }

    @Test
    public void testBuildRating() {
        Rating r = new RatingBuilder()
                .setUserId(692)
                .setItemId(483)
                .setRating(3.5)
                .setTimestamp(349702)
                .build();
        assertThat(r, notNullValue());
        assertThat(r.getUserId(), equalTo(692L));
        assertThat(r.getItemId(), equalTo(483L));
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(349702L));
    }
}
