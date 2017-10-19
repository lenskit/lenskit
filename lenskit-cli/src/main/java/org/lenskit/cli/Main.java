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

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.SystemUtils;
import org.lenskit.LenskitInfo;
import org.lenskit.cli.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Main entry point for lenskit-cli.
 *
 * @since 3.0
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newArgumentParser("lenskit")
                               .description("Work with LensKit recommenders and data.");
        Logging.addLoggingGroup(parser);

        Subparsers subparsers = parser.addSubparsers()
                                      .metavar("COMMAND")
                                      .title("commands");
        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        for (Command cmd: loader) {
            Subparser cp = subparsers.addParser(cmd.getName())
                                     .help(cmd.getHelp())
                                     .setDefault("command", cmd);
            cmd.configureArguments(cp);
        }

        try {
            Namespace options = parser.parseArgs(args);
            Logging.configureLogging(options);
            Runtime rt = Runtime.getRuntime();
            logger.info("Starting LensKit {} on Java {} from {}",
                        LenskitInfo.lenskitVersion(),
                        SystemUtils.JAVA_VERSION, SystemUtils.JAVA_VENDOR);
            logger.debug("Built from Git revision {}", LenskitInfo.getHeadRevision());
            logger.debug("Using VM '{}' version {} from {}",
                         SystemUtils.JAVA_VM_NAME,
                         SystemUtils.JAVA_VM_VERSION,
                         SystemUtils.JAVA_VM_VENDOR);
            logger.info("Have {} processors and heap limit of {} MiB",
                        rt.availableProcessors(), rt.maxMemory() >> 20);
            Command cmd = options.get("command");
            cmd.execute(options);
            logger.info("If you use LensKit in published research, please see http://lenskit.org/research/");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (LenskitCommandException e) {
            logger.error("error running command: " + e, e);
            System.exit(2);
        }
    }
}
