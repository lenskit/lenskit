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

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackHeaderFlagTest {
    @Test
    public void testPackEmptySet() throws Exception {
        assertThat(PackHeaderFlag.packWord(EnumSet.noneOf(PackHeaderFlag.class)),
                   equalTo((short) 0));
    }

    @Test
    public void testUnpackEmptySet() throws Exception {
        assertThat(PackHeaderFlag.unpackWord((short) 0), hasSize(0));
    }

    @Test
    public void testPackSingleElement() throws Exception {
        for (PackHeaderFlag flag: PackHeaderFlag.values()) {
            short word = PackHeaderFlag.packWord(EnumSet.of(flag));
            assertThat(word, equalTo((short) (1 << flag.ordinal())));
        }
    }

    @Test
    public void testUnpackSingleElement() throws Exception {
        for (int i = 0; i < PackHeaderFlag.values().length; i++) {
            Set<PackHeaderFlag> flags = PackHeaderFlag.unpackWord((short)(1 << i));
            assertThat(flags, contains(PackHeaderFlag.values()[i]));
        }
    }

    @Test
    public void testPackAllCompact() {
        EnumSet<PackHeaderFlag> flags = EnumSet.of(PackHeaderFlag.COMPACT_ITEMS,
                                                   PackHeaderFlag.COMPACT_USERS);
        int word = PackHeaderFlag.packWord(flags);
        assertThat(word, equalTo(6));
        assertThat(PackHeaderFlag.unpackWord((short) word),
                   equalTo(flags));
    }
}
