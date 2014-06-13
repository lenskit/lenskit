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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread that reads an input stream and writes its lines to a logger.  All lines are written at the
 * info level.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LoggingStreamSlurper extends Thread {
    private final BufferedReader reader;
    private final Logger logger;
    private final String prefix;

    /**
     * Create a new stream slurper.
     * @param name The thread name.
     * @param stream The input stream.
     * @param log The logger.
     * @param pfx A prefix to prepend to log messages.
     */
    public LoggingStreamSlurper(String name, InputStream stream, Logger log, String pfx) {
        super(name);
        reader = new BufferedReader(new InputStreamReader(stream));
        logger = log;
        prefix = pfx;
    }

    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                logger.info("{}{}", prefix, line);
            }
        } catch (IOException e) {
            logger.error("error reading from standard error", e);
        }
    }
}
