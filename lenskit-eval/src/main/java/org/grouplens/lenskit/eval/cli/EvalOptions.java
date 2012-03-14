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
package org.grouplens.lenskit.eval.cli;

import org.apache.commons.cli.*;
import org.grouplens.lenskit.eval.IsolationLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Parse & present command line options for running the evaluator.
 * @since 0.10
 * @author Michael Ekstrand
 */
public class EvalOptions {
    private Properties properties;
    private final boolean forcePrepare;
    private int threadCount = 1;
    private IsolationLevel isolation = IsolationLevel.NONE;
    private File cacheDir = new File(".eval-cache");
    private boolean prepareOnly;
    private boolean throwErrors;
    private List<File> configFiles;
    private boolean printBacktraces;

    private EvalOptions(CommandLine cmd) {
        properties = new Properties(System.getProperties());
        Properties cliprops = cmd.getOptionProperties("D");
        properties.putAll(cliprops);

        forcePrepare = cmd.hasOption("f");
        if (cmd.hasOption("j")) {
            threadCount = Integer.parseInt(cmd.getOptionValue("j"));
        }
        if (cmd.hasOption("isolate")) {
            isolation = IsolationLevel.JOB_GROUP;
        }
        if (cmd.hasOption("C")) {
            cacheDir = new File(cmd.getOptionValue("C"));
        }
        prepareOnly = cmd.hasOption("prepare-only");
        throwErrors = Boolean.parseBoolean(properties.getProperty("lenskit.eval.throwErrors", "false"));
        printBacktraces = cmd.hasOption("X");

        configFiles = new ArrayList<File>();
        for (String s: cmd.getArgs()) {
            configFiles.add(new File(s));
        }
    }

    public static EvalOptions parse(String... args) {
        CommandLineParser parser = new GnuParser();
        CommandLine line;
        Options options = makeOptions();
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        }
        if (line.hasOption("h")) {
            HelpFormatter fmt = new HelpFormatter();
            fmt.printHelp("lenskit-eval [OPTIONS] CONFIGS...", options);
            System.exit(1);
            return null;
        } else {
            return new EvalOptions(line);
        }
    }

    @SuppressWarnings({"static-access", "AccessStaticViaInstance"})
    private static Options makeOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder
                               .withDescription("print this help")
                               .withLongOpt("help")
                               .create("h"));
        opts.addOption(OptionBuilder
                               .withDescription("re-prepare data sets even if up to date")
                               .withLongOpt("force-prepare")
                               .create("f"));
        opts.addOption(OptionBuilder
                               .withDescription("the number of threads to use (0 to use all)")
                               .withLongOpt("threads")
                               .hasArg().withArgName("N")
                               .create("j"));
        opts.addOption(OptionBuilder
                               .withDescription("isolate job groups")
                               .withLongOpt("isolate")
                               .create());
        opts.addOption(OptionBuilder
                               .withDescription("directory for cache files")
                               .withLongOpt("cache-dir")
                               .hasArg().withArgName("DIR")
                               .create("C"));
        opts.addOption(OptionBuilder
                               .withDescription("set a property")
                               .withArgName("property=value")
                               .hasArgs(2)
                               .withValueSeparator()
                               .create("D"));
        opts.addOption(OptionBuilder
                               .withDescription("only prepare eval, do not run")
                               .withLongOpt("prepare-only")
                               .create());
        opts.addOption(OptionBuilder
                               .withDescription("throw exceptions rather than exiting")
                               .withLongOpt("throw-errors")
                               .create());
        opts.addOption("X", "print-backtraces", false,
                       "print backtraces on exceptions");

        return opts;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean forcePrepare() {
        return forcePrepare;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public IsolationLevel getIsolation() {
        return isolation;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public boolean isPrepareOnly() {
        return prepareOnly;
    }

    public boolean throwErrors() {
        return throwErrors;
    }

    public List<File> getConfigFiles() {
        return configFiles;
    }

    public boolean printBacktraces() {
        return printBacktraces;
    }
}
