package org.grouplens.lenskit.data.dao.packed;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Flags for the binary output format.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum BinaryFormatFlag {
    TIMESTAMPS;

    public static EnumSet<BinaryFormatFlag> makeSet(BinaryFormatFlag... flags) {
        EnumSet<BinaryFormatFlag> flagSet = EnumSet.noneOf(BinaryFormatFlag.class);
        flagSet.addAll(Arrays.asList(flags));
        return flagSet;
    }
}
