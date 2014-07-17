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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Flags for the binary output format.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum BinaryFormatFlag {
    /**
     * Include timestamps in the packed data.
     */
    TIMESTAMPS,
    /**
     * Do not use compact user and item IDs.  This can make packing more efficient if you know that
     * some user or item IDs do not fit in ints.
     */
    NO_COMPACT;

    public static EnumSet<BinaryFormatFlag> makeSet(BinaryFormatFlag... flags) {
        EnumSet<BinaryFormatFlag> flagSet = EnumSet.noneOf(BinaryFormatFlag.class);
        flagSet.addAll(Arrays.asList(flags));
        return flagSet;
    }
}
