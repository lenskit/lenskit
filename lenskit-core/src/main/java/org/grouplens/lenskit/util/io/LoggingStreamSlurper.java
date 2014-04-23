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
