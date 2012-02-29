/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.grouplens.lenskit.util.dtree.DataNode;
import org.grouplens.lenskit.util.dtree.Trees;
import org.grouplens.lenskit.util.dtree.xml.XMLDataNode;
import org.grouplens.lenskit.eval.EvalRunner;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.grouplens.lenskit.eval.IsolationLevel;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Main entry point to run the evaluator from the command line
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class EvalCLI {
    private static final Logger logger = LoggerFactory.getLogger(EvalCLI.class);
    
    /**
     * Run the evaluator from the command line.
     * @param args The command line arguments to the evaluator.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine line;
        Options options = makeOptions();
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }
        if (line.hasOption("h")) {
            HelpFormatter fmt = new HelpFormatter();
            fmt.printHelp("lenskit-eval [OPTIONS] CONFIGS...", options);
            System.exit(1);
        } else {
            EvalCLI cli = new EvalCLI(line);
            cli.run();
        }
    }
    
    protected final Properties properties;
    private boolean forcePrepare;
    private int threadCount = 1;
    private IsolationLevel isolation = IsolationLevel.NONE;
    private File cacheDir = new File(".eval-cache");
    private boolean prepareOnly;
    private boolean throwErrors;
    private List<File> configFiles;
    private boolean printBacktraces;
    
    public EvalCLI(CommandLine cmd) {
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
    
    @SuppressWarnings("static-access")
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
    
    public void run() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        List<EvalRunner> runners = new LinkedList<EvalRunner>();
        for (File f: configFiles) {
            Document doc;
            try {
                doc = builder.parse(f);
            } catch (SAXException e) {
                reportError(e, "%s: %s", f.getPath(), e.getMessage());
                return;
            } catch (IOException e) {
                reportError(e, "%s: %s\n", f.getPath(), e.getMessage());
                return;
            }
            
            DataNode node = XMLDataNode.wrap(properties, doc);
            if (!node.getName().equals("evaluation")) {
                reportError("%s: root element must be 'evaluation'", f.getPath());
            }
            
            String name = Trees.childValue(node, "type");
            if (name == null) {
                reportError("%s: no evaluation type specified\n", f.getPath());
            }
            
            logger.info("Configuring evaluation type {} from {}", name, f);
            try {
                runners.add(new EvalRunner(name, System.getProperties(), node));
            } catch (EvaluatorConfigurationException e) {
                reportError(e, "%s: %s\n", f.getPath(), e.getMessage());
                return;
            }
        }
        
        PreparationContext context = new PreparationContext();
        context.setUnconditional(forcePrepare);
        context.setCacheDirectory(cacheDir);
        for (EvalRunner runner: runners) {
            runner.setIsolationLevel(isolation);
            runner.setThreadCount(threadCount);
            try {
                runner.prepare(context);
            } catch (PreparationException e) {
                reportError(e, "Preparation error: " + e.getMessage());
                return;
            }
            
            if (!prepareOnly) {
                try {
                    runner.run();
                } catch (ExecutionException e) {
                    reportError(e, "Error running evaluation: %s", e.getMessage());
                    return;
                }
            }
        }
    }
    
    protected void reportError(String msg, Object... args) {
        reportError(null, msg, args);
    }
    
    protected void reportError(Exception e, String msg, Object... args) {
        String text = String.format(msg, args);
        System.err.println(text);
        if (throwErrors) {
            throw new RuntimeException(text, e);
        } else {
            if (e != null && printBacktraces)
                e.printStackTrace(System.err);
            System.exit(2);
        }
    }
}
