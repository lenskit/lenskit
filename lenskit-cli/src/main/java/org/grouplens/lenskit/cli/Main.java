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
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Main entry point for lenskit-cli.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Main {
    public static void main(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newArgumentParser("lenskit")
                               .description("Work with LensKit recommenders and data.");
        ArgumentGroup logging = parser.addArgumentGroup("logging")
                                      .description("Control the logging output.");
        logging.addArgument("--log-file")
               .type(File.class)
               .metavar("FILE")
               .help("write logging output to FILE");
        logging.addArgument("-d", "--debug")
               .action(Arguments.storeTrue())
               .help("write debug output to the console");
        logging.addArgument("--debug-grapht")
               .action(Arguments.storeTrue())
               .help("include debug output from Grapht");

        Subparsers subparsers = parser.addSubparsers()
                                      .metavar("COMMAND")
                                      .title("commands");
        registerClass(subparsers, Version.class);
        registerClass(subparsers, Eval.class);
        registerClass(subparsers, PackRatings.class);
        registerClass(subparsers, TrainModel.class);
        registerClass(subparsers, Recommend.class);
        registerClass(subparsers, Graph.class);

        try {
            Namespace options = parser.parseArgs(args);
            configureLogging(options);
            Command cmd = getCommand(options);
            cmd.execute();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }

    private static void registerClass(Subparsers subparsers, Class<? extends Command> cls) {
        CommandSpec spec = cls.getAnnotation(CommandSpec.class);
        if (spec == null) {
            throw new IllegalArgumentException(cls + " has no @CommandSpec annotation");
        }
        Subparser parser = subparsers.addParser(spec.name())
                                     .help(spec.help())
                                     .setDefault("command", cls);
        try {
            MethodUtils.invokeStaticMethod(cls, "configureArguments", parser);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("cannot configure command " + cls, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("cannot configure command " + cls, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("cannot configure command " + cls, e);
        }
    }

    public static Command getCommand(Namespace options) {
        Class<? extends Command> command = options.get("command");
        try {
            return ConstructorUtils.invokeConstructor(command, options);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("cannot instantiate command " + command, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("cannot instantiate command " + command, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("cannot instantiate command " + command, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("cannot instantiate command " + command, e);
        }
    }

    private static final String CONSOLE_PATTERN =
            "%highlight(%-5level) %white(%date{HH:mm:ss.SSS}) [%yellow(%thread)] " +
            "%cyan(%logger{24}) %msg%n";
    private static final String FILE_PATTERN =
            "%date{HH:mm:ss.SSS} %level [%thread] %logger: %msg%n";

    public static void configureLogging(Namespace options) {
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
            root.setLevel(Level.DEBUG);
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
