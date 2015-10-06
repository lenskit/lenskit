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
package org.lenskit.specs.eval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.lenskit.specs.DynamicSpec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prediction evaluation specification.
 */
public class PredictEvalTaskSpec extends EvalTaskSpec {
    private Path outputFile;
    private List<DynamicSpec> metrics = new ArrayList<>();

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

    @JsonIgnore
    @Override
    public Set<Path> getOutputFiles() {
        Set<Path> files = new HashSet<>();
        if (outputFile != null) {
            files.add(outputFile);
        }
        return files;
    }

    /**
     * Get the user prediction metrics.
     * @return The metrics
     */
    public List<DynamicSpec> getMetrics() {
        return metrics;
    }

    /**
     * Set the user prediction metrics.
     * @param ms The metrics.
     */
    public void setMetrics(List<DynamicSpec> ms) {
        metrics = ms;
    }

    /**
     * Add a user prediction metric.
     * @param metric The metric configuration.
     */
    public void addMetric(JsonNode metric) {
        metrics.add(new DynamicSpec(metric));
    }

    /**
     * Add a metric by name.  The metric will have no additional configuration.
     * @param metric The metric name.
     */
    public void addMetric(String metric) {
        addMetric(new TextNode(metric));
    }
}
