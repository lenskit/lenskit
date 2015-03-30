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
package org.lenskit.cli;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Interface implemented by all CLI subcommands.  Commands are detected by looking for
 * implementations of this interface using the Java {@link java.util.ServiceLoader} framework.
 * New implementations can be registered conveniently using the {@link com.google.auto.service.AutoService}
 * annotation.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface Command {
    /**
     * Get the name of the command.
     *
     * @return The command's name.
     */
    String getName();

    /**
     * Get the command's help.
     *
     * @return The command's help.
     */
    String getHelp();

    /**
     * Configure the argument parser for this command.
     * @param parser The argument parser into which the arguments should be configured.  This will already be a
     *               subparser, the command is not responsible for creating that.
     */
    void configureArguments(ArgumentParser parser);

    /**
     * Execute the command.
     *
     * @param options The command-line options.
     * @throws Exception if an error occurs in the command.
     */
    void execute(Namespace options) throws Exception;
}
