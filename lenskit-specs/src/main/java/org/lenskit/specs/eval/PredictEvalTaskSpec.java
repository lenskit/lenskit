package org.lenskit.specs.eval;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Prediction evaluation specification.
 */
public class PredictEvalTaskSpec extends EvalTaskSpec {
    private Path outputFile;

    /**
     * Get the prediction output file.
     * @return The prediction output file.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * Set the prediction output file.
     * @param outputFile The prediction output file.
     */
    public void setOutputFile(Path outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public Set<Path> getOutputFiles() {
        Set<Path> files = new HashSet<>();
        if (outputFile != null) {
            files.add(outputFile);
        }
        return files;
    }
}
