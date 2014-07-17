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
package org.grouplens.lenskit.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

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
        Logging.addLoggingGroup(parser);

        Subparsers subparsers = parser.addSubparsers()
                                      .metavar("COMMAND")
                                      .title("commands");
        registerClass(subparsers, Version.class);
        registerClass(subparsers, Eval.class);
        registerClass(subparsers, PackRatings.class);
        registerClass(subparsers, TrainModel.class);
        registerClass(subparsers, Recommend.class);
        registerClass(subparsers, Predict.class);
        registerClass(subparsers, Graph.class);

        try {
            Namespace options = parser.parseArgs(args);
            Logging.configureLogging(options);
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
}
