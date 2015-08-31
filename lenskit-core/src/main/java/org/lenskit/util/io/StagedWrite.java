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
package org.lenskit.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.UUID;

/**
 * Helper to do staged file writes.  In a staged write, code first writes the output to a temporary
 * file, then renames the temporary file on top of the target file. To use this file:
 *
 * ```java
 * try (StagedWrite stage = StagedWrite.begin(outputFile)) {
 *     try (OutputStream stream = stage.openOutputStream()) {
 *         // write to stream
 *     }
 *     stage.commit();
 * ```
 *
 * The rename operation is atomic, so outside code will either see the old target file (or lack thereof), or the entire
 * contents of the new target file; it will not see any intermediate states.  It is possible that this code will fail
 * on old versions of Windows, or on certain file systems; if you encounter problems with it, especially if it raises
 * {@link AtomicMoveNotSupportedException}, please file a bug report.
 */
public class StagedWrite implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(StagedWrite.class);
    private final Path targetFile;
    private final Path stagingFile;
    private boolean opened = false;
    private boolean committed = false;

    private StagedWrite(Path target, Path temp) {
        targetFile = target;
        stagingFile = temp;
    }

    /**
     * Begin a staged file writing operation.
     * @param target The file to write.
     * @return A staged file
     */
    public static StagedWrite begin(Path target) {
        UUID key = UUID.randomUUID();
        String stageName = ".tmp." + key + "." + target.getFileName().toString();
        Path stage = target.resolveSibling(stageName);
        return new StagedWrite(target, stage);
    }

    /**
     * Begin a staged file writing operation.
     * @param target The file to write.
     * @return A staged file
     */
    public static StagedWrite begin(File target) {
        return begin(target.toPath());
    }

    /**
     * Get the target file for this staging file.
     * @return The target file that will be written.
     */
    public Path getTargetFile() {
        return targetFile;
    }

    /**
     * Get the working file for this staging file.  Code doing staged file writes should write to
     * this file.
     * @return The working file.
     */
    public Path getStagingFile() {
        return stagingFile;
    }

    /**
     * Open an output stream for the staging file.  This method cannot be called multiple times.
     *
     * @return An output stream to write to the staging file. This output stream must be closed
     *         before calling {@link #commit()}.
     * @throws IOException if there is an error opening the output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        if (committed) {
            throw new IllegalStateException("staged write already committed");
        } else if (opened) {
            throw new IllegalStateException("staged write already opened");
        }
        OutputStream stream = Files.newOutputStream(stagingFile,
                                                    StandardOpenOption.WRITE,
                                                    StandardOpenOption.CREATE,
                                                    StandardOpenOption.CREATE_NEW);
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

        try {
            Files.move(stagingFile, targetFile,
                       StandardCopyOption.REPLACE_EXISTING,
                       StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            logger.error("file system does not support atomic moves", ex);
            logger.info("for more information, see: http://lenskit.org/master/apidocs/org/lenskit/util.io/StagedWrite.html");
            throw ex;
        }
        committed = true;
    }

    /**
     * Clean up the staged write, deleting the staging file if it still exists.  It is safe to call
     * this method multiple times, and safe to call it after calling {@link #commit()}.  Typical
     * use of a staged write will call this method in a {@code finally} block, or use the staged write
     * in a try-with-resources block.
     */
    @Override
    public void close() throws IOException {
        if (!committed) {
            logger.debug("aborting write of {}", targetFile);
        }
        Files.deleteIfExists(stagingFile);
    }
}
