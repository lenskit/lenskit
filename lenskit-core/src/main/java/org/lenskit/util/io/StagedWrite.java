/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
 * }
 * ```
 *
 * The rename operation is atomic, so outside code will either see the old target file (or lack thereof), or the entire
 * contents of the new target file; it will not see any intermediate states.  It is possible that this code will fail
 * on old versions of Windows, or on certain file systems; if you encounter problems with it, especially if it raises
 * {@link AtomicMoveNotSupportedException}, please file a bug report.
 */
public class StagedWrite implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(StagedWrite.class);
    /**
     * How many times will we retry a failed commit?
     */
    private static final int TRIES_MAX = 3;

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

        int ntries = 0;

        while (!committed) {
            ntries += 1;
            try {
                Files.move(stagingFile, targetFile,
                           StandardCopyOption.REPLACE_EXISTING,
                           StandardCopyOption.ATOMIC_MOVE);
                committed = true;
            } catch (AccessDeniedException ex) {
                // on Windows, we get access denied in certain race conditions
                // try again
                if (ntries >= TRIES_MAX) {
                    logger.debug("renaming {} failed too many times", targetFile);
                    throw ex;
                } else {
                    logger.debug("access denied committing {}, retrying", targetFile);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new InterruptedIOException("interrupted waiting for commit");
                    }
                }
            } catch (AtomicMoveNotSupportedException ex) {
                logger.error("file system does not support atomic moves", ex);
                logger.info("for more information, see: http://lenskit.org/master/apidocs/org/lenskit/util/io/StagedWrite.html");
                throw ex;
            }
        }
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
