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
package org.grouplens.lenskit.data.dao.packed;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test rendering with all possible combinations of flags.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@RunWith(Theories.class)
public class BinaryFormatRenderTest {
    @DataPoints
    public static BinaryFormat[] formats;

    @BeforeClass
    public static void makeFormats() {
        List<BinaryFormat> fmts = Lists.newArrayList();
        for (Set<PackHeaderFlag> flags: Sets.powerSet(EnumSet.allOf(PackHeaderFlag.class))) {
            fmts.add(BinaryFormat.create(flags.toArray(new PackHeaderFlag[flags.size()])));
        }
        formats = fmts.toArray(new BinaryFormat[fmts.size()]);
    }

    @Theory
    public void testRenderRating(BinaryFormat format) {
        long ts = format.hasTimestamps() ? 34208L : -1;
        Rating r = Ratings.make(42L, 39L, Math.PI, ts);
        ByteBuffer buf = ByteBuffer.allocate(format.getRatingSize());
        format.renderRating(r, buf);
        buf.flip();

        buf.mark();
        Rating r2 = format.readRating(buf);
        assertThat(r2, equalTo(r));

        buf.reset();
        Rating r3 = format.readRating(buf);
        assertThat(r3, equalTo(r));
    }
}
