package org.grouplens.lenskit.util.io;

import java.io.File;

/**
 * Utility class for detecting if a file is up-to-date.
 *
 * @author Michael Ekstrand
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
        addOutput(file.lastModified());
    }
}
