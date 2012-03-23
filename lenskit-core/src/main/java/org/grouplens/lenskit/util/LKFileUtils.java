/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

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

    /**
     * Close a set of closeable objects, swallowing and logging all exceptions.
     * @param log The logger to which to report errors.
     * @param toClose The objects to close.
     * @return {@code true} if all objects closed cleanly; {@code false} if some objects
     * failed when closing.
     */
    public static boolean close(Logger log, Closeable... toClose) {
        boolean success = true;
        for (Closeable c: toClose) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    String msg = String.format("error closing %s: %s", c, e);
                    log.error(msg, e);
                    success = false;
                } catch (RuntimeException e) {
                    String msg = String.format("error closing %s: %s", c, e);
                    log.error(msg, e);
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * Close a group of objects, using a logger extracted from the stack trace. Getting the
     * logger may be a tad slow, so if you have a logger use the other method.
     * @param toClose The objects to close.
     * @return {@code true} if all objects closed successfully.
     * @see #close(Logger, Closeable...)
     */
    public static boolean close(Closeable... toClose) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        Logger logger = null;
        int pos = 0;
        boolean foundMyself = false;
        while (logger == null && pos < trace.length) {
            StackTraceElement frame = trace[pos];
            if (foundMyself) {
                logger = LoggerFactory.getLogger(frame.getClassName());
            } else if (frame.getClassName().equals(LKFileUtils.class.getName())
                    && frame.getMethodName().equals("close")) {
                foundMyself = true;
            }
            pos += 1;
        }
        if (logger == null) {
            logger = LoggerFactory.getLogger(LKFileUtils.class);
        }
        return close(logger, toClose);
    }
}
