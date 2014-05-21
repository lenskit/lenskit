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
package org.grouplens.lenskit.cli;

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
               .help("include logging messages at LEVEL in log file");
        logging.addArgument("-d", "--debug")
               .action(Arguments.storeTrue())
               .help("include debug logging in console output");
        logging.addArgument("--debug-grapht")
               .action(Arguments.storeTrue())
               .help("include debug output from Grapht");
    }

    public static void configureLogging(Namespace options) {
        // if the user has explicitly configured a Logback config, honor that.
        if (System.getProperty("logback.configurationFile") != null) {
            return;
        }
        boolean debug = options.getBoolean("debug");
        boolean debugGrapht = options.getBoolean("debug_grapht");
        File logFile = options.get("log_file");

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

        if (logFile != null) {
            String lstr = options.getString("log_level");
            Level logLevel = Level.toLevel(lstr, Level.INFO);
            FileAppender<ILoggingEvent> fileOutput = new FileAppender<ILoggingEvent>();
            fileOutput.setContext(context);
            fileOutput.setFile(logFile.getAbsolutePath());
            PatternLayoutEncoder filePat = new PatternLayoutEncoder();
            filePat.setContext(context);
            filePat.setPattern(FILE_PATTERN);
            filePat.start();
            fileOutput.setEncoder(filePat);
            fileOutput.start();
            root.addAppender(fileOutput);
            root.setLevel(logLevel);
            if (!debug) {
                ThresholdFilter filter = new ThresholdFilter();
                filter.setContext(context);
                filter.setLevel("INFO");
                filter.start();
                console.addFilter(filter);
            }
        } else if (debug) {
            root.setLevel(Level.DEBUG);
        } else {
            root.setLevel(Level.INFO);
        }

        console.start();

        if (!debugGrapht) {
            context.getLogger("org.grouplens.grapht").setLevel(Level.WARN);
        }
    }
}
