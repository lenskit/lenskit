package org.lenskit.gradle;

import org.gradle.api.Task;

import java.io.File;

/**
 * Interface implemented by tasks that provide data sets for the train-test evaluator.
 */
public interface DataSetProvider extends Task {
    /**
     * Get the data set manifest file.
     *
     * @return The data set manifest file.
     */
    File getDataSetFile();
}
