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

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

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
        parser.addArgument("--log-file")
              .type(File.class)
              .metavar("FILE")
              .help("write logging output to FILE");
        parser.addArgument("-d", "--debug")
              .action(Arguments.storeTrue())
              .help("write debug output to the console");

        Subparsers subparsers = parser.addSubparsers()
                                      .metavar("COMMAND")
                                      .title("commands");
        registerClass(subparsers, "eval", Eval.class, "run an evaluation script");

        try {
            Namespace options = parser.parseArgs(args);
            // FIXME Set up and use logging
            Callable<Void> cmd = getCommand(options);
            cmd.call();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }

    private static void registerClass(Subparsers subparsers, String name,
                                      Class<? extends Callable<Void>> cls, String help) {
        Subparser parser = subparsers.addParser(name)
                                     .help(help)
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

    public static Callable<Void> getCommand(Namespace options) {
        Class<? extends Callable<Void>> command = options.get("command");
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
}
