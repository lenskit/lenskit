/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.io;

import java.io.File;

/**
 * Utility class for detecting if a file is up-to-date.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class UpToDateChecker {
    private long newestSource = Long.MIN_VALUE;
    private long oldestOutput = Long.MAX_VALUE;

    /**
     * Query if the output is up to date.
     *
     * @return {@code true} if all outputs are up to date with respect to the inputs.
     */
    public boolean isUpToDate() {
        return oldestOutput > newestSource;
    }

    /**
     * Query if a particular output is up to date.
     *
     * @param ts An output timestamp.
     * @return {@code true} if an output with modification time {@var ts} is up to date
     *         with respect to the input resources.
     */
    public boolean isUpToDate(long ts) {
        return ts > newestSource;
    }

    /**
     * Add an input timestamp.
     *
     * @param ts The modification time of an input source, in milliseconds since the epoch.
     */
    public void addInput(long ts) {
        if (ts > newestSource) {
            newestSource = ts;
        }
    }

    /**
     * Add an input file.
     *
     * @param file The input file.
     */
    public void addInput(File file) {
        addInput(file.lastModified());
    }

    /**
     * Add an output timestamp.
     *
     * @param ts An output timestamp.
     */
    public void addOutput(long ts) {
        oldestOutput = ts;
    }
    /**
     * Add an output file to check.
     *
     * @param file The file to add to the output files to check.
     */
    public void addOutput(File file) {
        long lm = file.lastModified();
        if (lm == 0) {
            // file doesn't exist, pretend it's indefinitely old
            lm = Long.MIN_VALUE;
        }
        addOutput(lm);
    }
}
