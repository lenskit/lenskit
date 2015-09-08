package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.eval.traintest.TrainTestExperiment;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.TrainTestExperimentSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@AutoService(Command.class)
public class TrainTest implements Command {
    private final Logger logger = LoggerFactory.getLogger(TrainTest.class);

    @Override
    public String getName() {
        return "train-test";
    }

    @Override
    public String getHelp() {
        return "Run a train-test evaluation of algorithms over data sets.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("specFile")
              .metavar("SPEC")
              .help("Load train-test configuration from SPEC")
              .type(File.class)
              .required(true);
    }

    private File getSpecFile(Namespace options) {
        return options.get("specFile");
    }

    @Override
    public void execute(Namespace options) throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(options);
        File specFile = getSpecFile(options);
        logger.info("loading train-test configuration from {}", specFile);
        // FIXME Use the class loader
        TrainTestExperimentSpec spec = SpecUtils.load(TrainTestExperimentSpec.class, specFile.toPath());
        TrainTestExperiment experiment = TrainTestExperiment.fromSpec(spec);
        experiment.execute();
    }
}
