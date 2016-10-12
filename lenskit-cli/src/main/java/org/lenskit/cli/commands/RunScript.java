/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import com.google.common.io.Files;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

@AutoService(Command.class)
public class RunScript implements Command {
    private static final Logger logger = LoggerFactory.getLogger(RunScript.class);

    @Override
    public String getName() {
        return "run-script";
    }

    @Override
    public String getHelp() {
        return "Run a Groovy script against a LensKit model.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.addArgument("script")
              .type(File.class)
              .metavar("SCRIPT")
              .help("run SCRIPT");
        parser.addArgument("args")
              .type(String.class)
              .nargs("*")
              .metavar("ARGS")
              .help("pass ARGS to script");
    }

    @Override
    public void execute(Namespace options) throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        File scriptFile = options.get("script");
        String ext = Files.getFileExtension(scriptFile.getName());
        ScriptEngine engine = sem.getEngineByExtension(ext);
        logger.info("running {} with engine {}", scriptFile, engine);
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("logger", LoggerFactory.getLogger(scriptFile.getName()));
        bindings.put("args", options.<List<String>>get("args"));
        try (Reader reader = new FileReader(scriptFile)) {
            engine.eval(reader, bindings);
        }
    }
}
