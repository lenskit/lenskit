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
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;

/**
 * Parse & present command line options for running the evaluator.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
public class EvalCLIOptions {
    private static final Logger logger = LoggerFactory.getLogger(EvalCLIOptions.class);

    private String[] args;
    private File configFile = new File("eval.groovy");
    private URL[] classpathUrls;
    private Properties props;
    private boolean force;
    private int nthreads = -1;

    private EvalCLIOptions(CommandLine cmd) {
        String[] cpadds = cmd.getOptionValues("C");
        if (cpadds != null) {
            classpathUrls = new URL[cpadds.length];
            for (int i = 0; i < cpadds.length; i++) {
                URL url = null;
                try {
                    File f = new File(cpadds[i]);
                    url = f.toURI().toURL();
                } catch (MalformedURLException e) {
                    logger.error("malformed classpath URL {}", url);
                    throw new RuntimeException("invalid classpath entry", e);
                }
                logger.info("adding {} to classpath", url);
                classpathUrls[i] = url;
            }
        }

        if (cmd.hasOption("f")) {
            configFile = new File(cmd.getOptionValue("f"));
        }
        props = cmd.getOptionProperties("D");
        force = cmd.hasOption("F");
        if (cmd.hasOption("j")) {
            String n = cmd.getOptionValue("j");
            if (n == null) {
                nthreads = Runtime.getRuntime().availableProcessors();
            } else {
                nthreads = Integer.parseInt(n);
            }
        }

        args = cmd.getArgs();
    }

    public static EvalCLIOptions parse(String... args) {
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
            return new EvalCLIOptions(line);
        }
    }

    @SuppressWarnings({"static-access", "AccessStaticViaInstance"})
    private static Options makeOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.withDescription("print this help")
                .withLongOpt("help")
                .create("h"));
        opts.addOption(OptionBuilder.withDescription("force eval tasks to run")
                .withLongOpt("force")
                .create("F"));
        opts.addOption(OptionBuilder.withDescription("number of threads to use")
                .hasOptionalArg().withArgName("N")
                .withLongOpt("thread-count")
                .create("j"));
        opts.addOption(OptionBuilder.withDescription("specify the eval configuration script")
                .hasArg().withArgName("FILE")
                .create("f"));
        opts.addOption(OptionBuilder.withDescription("add a JAR or directory to the classpath")
                .withLongOpt("add-to-classpath")
                .hasArg()
                .create("C"));
        opts.addOption(OptionBuilder.withDescription("throw exceptions rather than exiting")
                .withLongOpt("throw-errors")
                .create());
        opts.addOption(OptionBuilder.withDescription("define a property")
                .withArgName("property=value")
                .withValueSeparator()
                .hasArgs(2)
                .create("D"));
        return opts;
    }


    public boolean throwErrors() {
        return Boolean.parseBoolean(System.getProperty("lenskit.eval.throwErrors", "false"));
    }

    public File getScriptFile() {
        return configFile;
    }

    public String[] getArgs() {
        return args;
    }

    public ClassLoader getClassLoader(ClassLoader parent) {
        if (classpathUrls == null) {
            return parent;
        } else {
            return new URLClassLoader(classpathUrls, parent);
        }
    }

    public Properties getProperties() {
        Properties ps = new Properties(System.getProperties());
        for (Map.Entry<Object, Object> e: props.entrySet()) {
            ps.setProperty((String) e.getKey(), (String) e.getValue());
        }
        if (force) {
            ps.setProperty(EvalConfig.FORCE_PROPERTY, "true");
        }
        if (nthreads >= 0) {
            ps.setProperty(EvalConfig.THREAD_COUNT_PROPERTY, Integer.toString(nthreads));
        }
        return ps;
    }

    public ClassLoader getClassLoader() {
        return getClassLoader(Thread.currentThread().getContextClassLoader());
    }

}
