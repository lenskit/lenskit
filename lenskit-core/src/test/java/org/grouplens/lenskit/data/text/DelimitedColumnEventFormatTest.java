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
package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class DelimitedColumnEventFormatTest {
    DelimitedColumnEventFormat<Rating,RatingBuilder> format;

    @Before
    public void createFormat() {
        format = new DelimitedColumnEventFormat<Rating,RatingBuilder>(new RatingEventType());
    }

    @Test
    public void testCreateForRatings() {
        DelimitedColumnEventFormat fmt =
                DelimitedColumnEventFormat.create("rating");
        assertThat(fmt.getEventTypeDefinition(),
                   instanceOf(RatingEventType.class));
    }

    @Test
    public void parseBasicTSV() throws InvalidRowException {
        Rating r = format.parse("42\t39\t3.5");
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(39L));
        assertThat(r.getPreference(), notNullValue());
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void parseBasicCSV() throws InvalidRowException {
        format.setDelimiter(",");
        Rating r = format.parse("42,39,3.5");
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(39L));
        assertThat(r.getPreference(), notNullValue());
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void parseBasicDoubleColon() throws InvalidRowException {
        format.setDelimiter("::");
        Rating r = format.parse("42::39::3.5");
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(39L));
        assertThat(r.getPreference(), notNullValue());
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void parseTimestamp() throws InvalidRowException {
        format.setDelimiter("::");
        Rating r = format.parse("42::39::3.5::3490298");
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(39L));
        assertThat(r.getPreference(), notNullValue());
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(3490298L));
    }
}
