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
package org.lenskit.cli.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Logging {
    private Logging() {}

    private static final String CONSOLE_PATTERN =
            "%highlight(%-5level) %white(%date{HH:mm:ss.SSS}) [%yellow(%thread)] " +
            "%cyan(%logger{24}) %msg%n";
    private static final String FILE_PATTERN =
            "%date{HH:mm:ss.SSS} %level [%thread] %logger: %msg%n";

    public static void addLoggingGroup(ArgumentParser parser) {
        ArgumentGroup logging = parser.addArgumentGroup("logging")
                                      .description("Control the logging output.");
        logging.addArgument("--log-file")
               .type(File.class)
               .metavar("FILE")
               .help("write logging output to FILE");
        logging.addArgument("--log-level")
               .type(String.class)
               .metavar("LEVEL")
               .help("include logging messages at LEVEL in log output");
        logging.addArgument("--log-file-level")
               .type(String.class)
               .metavar("LEVEL")
               .help("include logging messages at LEVEL in log file (defaults to --log-level value)");
        logging.addArgument("--debug-grapht")
               .action(Arguments.storeTrue())
               .help("include debug output from Grapht");
    }

    public static void configureLogging(Namespace options) {
        // if the user has explicitly configured a Logback config, honor that.
        if (System.getProperty("logback.configurationFile") != null) {
            return;
        }
        boolean debugGrapht = options.getBoolean("debug_grapht");
        File logFile = options.get("log_file");

        String lstr = options.getString("log_level");
        Level logLevel = Level.toLevel(lstr, Level.INFO);


        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();

        ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<ILoggingEvent>();
        console.setContext(context);
        console.setTarget("System.err");
        console.setWithJansi(true);
        PatternLayoutEncoder consolePat = new PatternLayoutEncoder();
        consolePat.setContext(context);
        consolePat.setPattern(CONSOLE_PATTERN);
        consolePat.start();
        console.setEncoder(consolePat);
        root.addAppender(console);

        Level rootLevel = logLevel;

        if (logFile != null) {
            // sort out the log level situation
            String lfstr = options.getString("log_file_level");
            Level logFileLevel = Level.toLevel(lfstr, logLevel);

            if (!logFileLevel.equals(logLevel)) {
                // filter the console log
                ThresholdFilter filter = new ThresholdFilter();
                filter.setContext(context);
                filter.setLevel(logLevel.toString());
                filter.start();
                console.addFilter(filter);

                // root level needs to be decreased
                if (logLevel.isGreaterOrEqual(logFileLevel)) {
                    rootLevel = logFileLevel;
                }
            }

            FileAppender<ILoggingEvent> fileOutput = new FileAppender<>();
            fileOutput.setAppend(false);
            fileOutput.setContext(context);
            fileOutput.setFile(logFile.getAbsolutePath());
            PatternLayoutEncoder filePat = new PatternLayoutEncoder();
            filePat.setContext(context);
            filePat.setPattern(FILE_PATTERN);
            filePat.start();
            fileOutput.setEncoder(filePat);

            // filter the file output
            ThresholdFilter filter = new ThresholdFilter();
            filter.setContext(context);
            filter.setLevel(logFileLevel.toString());
            filter.start();
            fileOutput.addFilter(filter);

            fileOutput.start();

            root.addAppender(fileOutput);
        }

        // set root level to min needed to pass a filter
        root.setLevel(rootLevel);

        console.start();

        // tone down Grapht logging
        if (!debugGrapht) {
            context.getLogger("org.grouplens.grapht").setLevel(Level.WARN);
        }
    }
}
