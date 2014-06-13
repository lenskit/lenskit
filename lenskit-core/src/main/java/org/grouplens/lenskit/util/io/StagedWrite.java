/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import java.io.Closeable;
import java.io.File;
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
 *     OutputStream stream = stage.openOutputStream();
 *     // write to stream
 *     stream.close();
 *     stage.commit();
 * } finally {
 *     stage.close();
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
public class StagedWrite implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(StagedWrite.class);
    private final File targetFile;
    private final File stagingFile;
    private boolean opened = false;
    private boolean committed = false;

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

    /**
     * Open an output stream for the staging file.  This method cannot be called multiple times.
     *
     * @return An output stream to write to the staging file. This output stream must be closed
     *         before calling {@link #commit()}.
     * @throws IOException if there is an error opening the output stream.
     */
    public FileOutputStream openOutputStream() throws IOException {
        if (committed) {
            throw new IllegalStateException("staged write already committed");
        } else if (opened) {
            throw new IllegalStateException("staged write already opened");
        }
        FileOutputStream stream = new FileOutputStream(stagingFile);
        opened = true;
        return stream;
    }

    /**
     * Complete the staging write by replacing the target file with the staging file.  Any streams
     * or channels used to write the file must be closed and/or flushed prior to calling this method.
     * @throws IOException if there is an error moving the file.
     */
    public void commit() throws IOException {
        if (committed) {
            throw new IllegalStateException("staged write already committed");
        }
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
        committed = true;
    }

    /**
     * Clean up the staged write, deleting the staging file if it still exists.  It is safe to call
     * this method multiple times, and safe to call it after calling {@link #commit()}.  Typical
     * use of a staged write will call this method in a {@code finally} block.
     */
    @Override
    public void close() {
        if (!committed) {
            logger.debug("aborting write of {}", targetFile);
        }
        if (stagingFile.delete()) {
            logger.debug("deleted staging file {}", stagingFile);
        }
    }
}
