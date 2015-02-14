package org.grouplens.lenskit.cli;

import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineBuilder;
import org.grouplens.lenskit.core.LenskitRecommenderEngineLoader;
import org.grouplens.lenskit.data.dao.ItemNameDAO;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Load recommenders from models or configurations.
 */
public class RecommenderLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InputData input;
    private final ScriptEnvironment environment;
    private final Namespace options;

    public RecommenderLoader(InputData in, ScriptEnvironment env, Namespace opts) {
        input = in;
        environment = env;
        options = opts;
    }

    static void configureArguments(ArgumentParser parser) {
        parser.addArgument("-c", "--config-file")
              .type(File.class)
              .action(Arguments.append())
              .metavar("FILE")
              .help("use configuration from FILE");
        parser.addArgument("-m", "--model-file")
              .type(File.class)
              .metavar("FILE")
              .help("load model from FILE");
    }

    public List<File> getConfigFiles() {
        return options.getList("config_file");
    }

    LenskitRecommenderEngine loadEngine() throws RecommenderBuildException, IOException {
        LenskitConfiguration roots = new LenskitConfiguration();
        roots.addRoot(ItemNameDAO.class);
        File modelFile = options.get("model_file");
        if (modelFile == null) {
            logger.info("creating fresh recommender");
            LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                builder.addConfiguration(config);
            }
            builder.addConfiguration(input.getConfiguration());
            builder.addConfiguration(roots);
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine = builder.build();
            timer.stop();
            logger.info("built recommender in {}", timer);
            return engine;
        } else {
            logger.info("loading recommender from {}", modelFile);
            LenskitRecommenderEngineLoader loader = LenskitRecommenderEngine.newLoader();
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                loader.addConfiguration(config);
            }
            loader.addConfiguration(input.getConfiguration());
            loader.addConfiguration(roots);
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine;
            InputStream input = new FileInputStream(modelFile);
            try {
                input = CompressionMode.autodetect(modelFile).wrapInput(input);
                engine = loader.load(input);
            } finally {
                input.close();
            }
            timer.stop();
            logger.info("loaded recommender in {}", timer);
            return engine;
        }
    }
}
