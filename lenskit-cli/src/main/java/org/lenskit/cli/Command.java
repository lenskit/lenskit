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
    void execute(Namespace options) throws LenskitCommandException;
}
