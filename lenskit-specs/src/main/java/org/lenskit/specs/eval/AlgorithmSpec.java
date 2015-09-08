package org.lenskit.specs.eval;

import org.lenskit.specs.AbstractSpec;

import java.nio.file.Path;

/**
 * Specification for an agorithm.
 */
public class AlgorithmSpec extends AbstractSpec {
    private String name;
    private Path configFile;

    /**
     * Get the algorithm's name.
     * @return The algorithm's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the algorithm's name.
     * @param n The algorithm's name.
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Get the algorithm's configuration file.
     * @return The algorithm's configuration file.
     */
    public Path getConfigFile() {
        return configFile;
    }

    /**
     * Set the algorithm's configuration file.
     * @param file The algorithm's configuration file.
     */
    public void setConfigFile(Path file) {
        configFile = file;
    }
}
