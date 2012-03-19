package org.grouplens.lenskit.util;

import java.io.File;

/**
 * File utilities for LensKit. Called LKFileUtils to avoid conflict with FileUtils
 * classes that may be imported from other packages such as Guava, Plexus, or Commons.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
public final class LKFileUtils {
    private LKFileUtils() {}

    /**
     * Query whether this filename represents a compressed file. It just looks at
     * the name to see if it ends in “.gz”.
     * @param file The file to query.
     * @return {@code true} if the file name ends in “.gz”.
     */
    public static boolean isCompressed(File file) {
        return file.getName().endsWith(".gz");
    }
}
