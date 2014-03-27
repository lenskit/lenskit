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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Helper to do staged file writes.  In a staged write, code first writes the output to a temporary
 * file, then renames the temporary file on top of the target file. To use this file:
 *
 * <pre>{@code
 * StagedWrite stage = StagedWrite.begin(outputFile);
 * try {
 *     OutputStream stream = new FileOutputStream(stage.getStagingFile());
 *     // write to stream
 *     stream.close();
 *     stage.commit();
 * } finally {
 *     stage.cleanup();
 * }}</pre>
 *
 * <p>
 * The logic used to implement this class is subject to race conditions, so it should not be used
 * when multiple threads or processes may attempt to write the same file.  In LensKit 3.0, the race
 * condition will be removed on systems exposing POSIX file system semantics, but the APIs needed
 * for us to provide that capability are not present on Java 6.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class StagedWrite {
    private static final Logger logger = LoggerFactory.getLogger(StagedWrite.class);
    private final File targetFile;
    private final File stagingFile;

    private StagedWrite(File target, File temp) {
        targetFile = target;
        stagingFile = temp;
    }

    /**
     * Begin a staged file writing operation.
     * @param target The file to write.
     * @return A staged file
     */
    public static StagedWrite begin(File target) {
        File dir = target.getParentFile();
        UUID key = UUID.randomUUID();
        String stageName = ".tmp." + key + "." + target.getName();
        File stage = new File(dir, stageName);
        return new StagedWrite(target, stage);
    }

    /**
     * Get the target file for this staging file.
     * @return The target file that will be written.
     */
    public File getTargetFile() {
        return targetFile;
    }

    /**
     * Get the working file for this staging file.  Code doing staged file writes should write to
     * this file.
     * @return The working file.
     */
    public File getStagingFile() {
        return stagingFile;
    }

    public FileOutputStream openOutputStream() throws FileNotFoundException {
        return new FileOutputStream(stagingFile);
    }

    /**
     * Complete the staging write by replacing the target file with the staging file.
     * @throws IOException if there is an error moving the file.
     */
    public void commit() throws IOException {
        logger.debug("finishing write of {}", targetFile);
        if (!stagingFile.renameTo(targetFile)) {
            logger.debug("cannot rename staging file {}, trying to delete", stagingFile);
            // FIXME This is racy - in LensKit 3.0, replace with Files.move
            if (!targetFile.delete()) {
                logger.debug("cannot delete {}", targetFile);
                throw new IOException("cannot delete " + targetFile);
            }
            if (!stagingFile.renameTo(targetFile)) {
                logger.debug("cannot rename {} in second attempt", targetFile);
                throw new IOException("failed to create " + targetFile);
            }
        }
    }

    /**
     * Clean up the staged write, deleting the staging file if it still exists.
     */
    public void cleanup() {
        logger.debug("aborting write of {}", targetFile);
        if (stagingFile.delete()) {
            logger.debug("deleted staging file {}", stagingFile);
        }
    }
}
