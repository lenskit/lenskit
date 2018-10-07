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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import com.google.common.io.Files;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.RecommenderLoader;
import org.lenskit.cli.util.ScriptEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        parser.description("Predicts a user's rating of some items.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        RecommenderLoader.configureArguments(parser);
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
    public void execute(Namespace options) throws LenskitCommandException {
        ScriptEnvironment env = new ScriptEnvironment(options);
        InputData input = new InputData(env, options);
        RecommenderLoader loader = new RecommenderLoader(input, env, options);

        LenskitRecommenderEngine engine = null;
        try {
            engine = loader.loadEngine();
        } catch (IOException e) {
            throw new LenskitCommandException(e);
        }
        LenskitRecommender rec = engine.createRecommender(input.getDAO());

        ScriptEngineManager sem = new ScriptEngineManager();
        File scriptFile = options.get("script");
        String ext = Files.getFileExtension(scriptFile.getName());
        ScriptEngine seng = sem.getEngineByExtension(ext);
        logger.info("running {} with engine {}", scriptFile, seng);
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("logger", LoggerFactory.getLogger(scriptFile.getName()));
        bindings.put("args", options.<List<String>>get("args"));
        bindings.put("recommender", rec);
        try (Reader reader = new FileReader(scriptFile)) {
            seng.eval(reader, bindings);
        } catch (IOException e) {
            throw new LenskitCommandException("error loading script file", e);
        } catch (ScriptException e) {
            throw new LenskitCommandException("script evaluation failed", e);
        }
    }
}
