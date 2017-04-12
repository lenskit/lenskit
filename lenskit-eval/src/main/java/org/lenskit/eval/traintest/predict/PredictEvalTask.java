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
package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.util.ClassLoaders;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.api.*;
import org.lenskit.eval.traintest.*;
import org.lenskit.eval.traintest.metrics.Metric;
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;
import org.lenskit.util.table.writer.CSVWriter;
import org.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * An eval task that attempts to predict the user's test ratings.  It is generally a bad idea, as well as pointless,
 * to have multiple predict tasks in a single experiment.
 */
public class PredictEvalTask implements EvalTask {
    private static final Logger logger = LoggerFactory.getLogger(PredictEvalTask.class);
    static final PredictMetric<?>[] DEFAULT_METRICS = {
            new CoveragePredictMetric(),
            new MAEPredictMetric(),
            new RMSEPredictMetric()
    };

    private Path outputFile;
    private List<PredictMetric<?>> predictMetrics = Lists.newArrayList(DEFAULT_METRICS);

    private ExperimentOutputLayout experimentOutputLayout;
    private TableWriter outputTable;

    /**
     * Create a new eval task.
     */
    public PredictEvalTask() {
    }

    /**
     * Create a predict eval task from a JSON/YAML file.
     * @param json The task specification.
     * @param base The base URI from which `json` came, used for resolving relative paths.
     * @return The task.
     */
    public static PredictEvalTask fromJSON(JsonNode json, URI base) throws IOException {
        PredictEvalTask task = new PredictEvalTask();
        String outFile = json.path("output_file").asText(null);
        if (outFile != null) {
            task.setOutputFile(Paths.get(base.resolve(outFile)));
        }

        MetricLoaderHelper mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(PredictEvalTask.class),
                                                        "predict-metrics");
        JsonNode metricNode = json.get("metrics");
        if (metricNode != null && !metricNode.isNull()) {
            task.predictMetrics.clear();
            for (JsonNode mn : metricNode) {
                PredictMetric metric = mlh.createMetric(PredictMetric.class, mn);
                task.addMetric(metric);
            }
        }

        return task;
    }

    /**
     * Get the output file for writing predictions.
     * @return The output file, or {@code null} if no file is configured.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * Set the output file for predictions.
     * @param file The output file for writing predictions. Will get a CSV file.
     */
    public void setOutputFile(Path file) {
        outputFile = file;
    }

    /**
     * Get the list of prediction metrics.
     * @return The list of prediction metrics.  This list is live, not copied, so it can be modified or cleared.
     */
    public List<PredictMetric<?>> getPredictMetrics() {
        return predictMetrics;
    }

    /**
     * Get the list of all metrics.
     * @return A list containing all metrics used by this task.
     */
    public List<Metric<?>> getAllMetrics() {
        ImmutableList.Builder<Metric<?>> metrics = ImmutableList.builder();
        metrics.addAll(predictMetrics);
        return metrics.build();
    }

    /**
     * Add a prediction metric.
     * @param metric The metric to add.
     */
    public void addMetric(PredictMetric<?> metric) {
        predictMetrics.add(metric);
    }

    @Override
    public Set<Class<?>> getRequiredRoots() {
        return FluentIterable.from(getAllMetrics())
                             .transformAndConcat(new Function<Metric<?>, Iterable<Class<?>>>() {
                                 @Nullable
                                 @Override
                                 public Iterable<Class<?>> apply(Metric<?> input) {
                                     return input.getRequiredRoots();
                                 }
                             }).toSet();
    }

    @Override
    public List<String> getGlobalColumns() {
        ImmutableList.Builder<String> columns = ImmutableList.builder();
        for (Metric<?> m: getAllMetrics()) {
            columns.addAll(m.getAggregateColumnLabels());
        }
        return columns.build();
    }

    @Override
    public List<String> getUserColumns() {
        ImmutableList.Builder<String> columns = ImmutableList.builder();
        for (PredictMetric<?> pm: getPredictMetrics()) {
            columns.addAll(pm.getColumnLabels());
        }
        return columns.build();
    }

    @Override
    public void start(ExperimentOutputLayout outputLayout) {
        experimentOutputLayout = outputLayout;
        Path outFile = getOutputFile();
        if (outFile == null) {
            return;
        }

        TableLayoutBuilder tlb = TableLayoutBuilder.copy(outputLayout.getConditionLayout());
        TableLayout layout = tlb.addColumn("User")
                                .addColumn("Item")
                                .addColumn("Rating")
                                .addColumn("Prediction")
                                .build();
        try {
            logger.info("writing predictions to {}", outFile);
            outputTable = CSVWriter.open(outFile.toFile(), layout, CompressionMode.AUTO);
        } catch (IOException e) {
            throw new EvaluationException("error opening prediction output file", e);
        }
    }

    @Override
    public void finish() {
        experimentOutputLayout = null;
        if (outputTable != null) {
            try {
                outputTable.close();
                outputTable = null;
            } catch (IOException e) {
                throw new EvaluationException("error closing prediction output file", e);
            }
        }
    }

    @Override
    public ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        Preconditions.checkState(experimentOutputLayout != null, "experiment not started");
        TableWriter tlb = experimentOutputLayout.prefixTable(outputTable, dataSet, algorithm);

        List<MetricContext<?>> predictContexts = new ArrayList<>(predictMetrics.size());
        for (PredictMetric<?> metric: predictMetrics) {
            predictContexts.add(MetricContext.create(metric, algorithm, dataSet, engine));
        }

        return new PredictConditionEvaluator(tlb, predictContexts);
    }

    static class MetricContext<X> {
        final PredictMetric<X> metric;
        final X context;

        public MetricContext(PredictMetric<X> m, X ctx) {
            metric = m;
            context = ctx;
        }

        @Nonnull
        public MetricResult measureUser(TestUser user, ResultMap predictions) {
            return metric.measureUser(user, predictions, context);
        }

        @Nonnull
        public MetricResult getAggregateMeasurements() {
            return metric.getAggregateMeasurements(context);
        }

        /**
         * Create a new metric context. Indirected through this method to help the type checker.
         */
        public static <X> MetricContext<X> create(PredictMetric<X> metric, AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
            X ctx = metric.createContext(algorithm, dataSet, engine);
            return new MetricContext<>(metric, ctx);
        }
    }

    class PredictConditionEvaluator implements ConditionEvaluator {
        private final TableWriter writer;
        private final List<MetricContext<?>> predictMetricContexts;

        public PredictConditionEvaluator(TableWriter tw, List<MetricContext<?>> mcs) {
            writer = tw;
            predictMetricContexts = mcs;
        }

        @Nonnull
        @Override
        public Map<String, Object> measureUser(Recommender recommender, TestUser testUser) {
            RatingPredictor predictor = recommender.getRatingPredictor();
            if (predictor == null) {
                logger.debug("recommender cannot predict ratings");
                return Collections.emptyMap();
            }
            Long2DoubleMap ratings = testUser.getTestRatings();
            ResultMap results = predictor.predictWithDetails(testUser.getUserId(), ratings.keySet());

            // Measure the user results
            Map<String,Object> row = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                row.putAll(mc.measureUser(testUser, results).getValues());
            }

            // Write all attempted predictions
            for (Long2DoubleMap.Entry e: ratings.long2DoubleEntrySet()) {
                Result pred = results.get(e.getLongKey());
                try {
                    if (writer != null) {
                        writer.writeRow(testUser.getUserId(), e.getLongKey(), e.getDoubleValue(),
                                        pred != null ? pred.getScore() : null);
                    }
                } catch (IOException ex) {
                    throw new EvaluationException("error writing prediction row", ex);
                }
            }

            return row;
        }

        @Nonnull
        @Override
        public Map<String, Object> finish() {
            Map<String,Object> results = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                MetricResult measurements = mc.getAggregateMeasurements();
                // if test and train tests are disjoint we can get "null"
                if (measurements != null) {
                    results.putAll(measurements.getValues());
                } else {
                    logger.warn("Metric {} returned null results", mc.metric);
                }
            }
            return results;
        }
    }
}
