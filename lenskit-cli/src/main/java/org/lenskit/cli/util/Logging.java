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
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * CLI support for configuring the logging infrastructure.
 *
 * @see org.lenskit.cli.Main
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

        // manually run JAnsi setup to use its terminal detection logic
        AnsiConsole.systemInstall();
        ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
        console.setContext(context);
        console.setTarget("System.err");
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
