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
package org.lenskit.eval.traintest.recommend;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.*;
import org.lenskit.eval.traintest.metrics.Metric;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.specs.DynamicSpec;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.RecommendEvalTaskSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An eval task that attempts to recommend items for a test user.
 */
public class RecommendEvalTask implements EvalTask {
    private static final Logger logger = LoggerFactory.getLogger(RecommendEvalTask.class);
    private static final TopNMetric<?>[] DEFAULT_METRICS = {
            new TopNLengthMetric()
    };

    private ExperimentOutputLayout experimentOutputLayout;
    private Path outputFile;
    private TableWriter outputTable;
    private List<TopNMetric<?>> topNMetrics = Lists.newArrayList(DEFAULT_METRICS);
    private int listSize = 10;
    private ItemSelector candidateSelector = ItemSelector.nullSelector();

    /**
     * Create a top-N eval task from a specification.
     * @param ets The task specification.
     * @return The task.
     */
    public static RecommendEvalTask fromSpec(RecommendEvalTaskSpec ets) {
        RecommendEvalTask task = new RecommendEvalTask();
        task.setOutputFile(ets.getOutputFile());
        task.setListSize(ets.getListSize());
        if (!ets.getMetrics().isEmpty()) {
            task.getTopNMetrics().clear();
            for (DynamicSpec ms: ets.getMetrics()) {
                TopNMetric<?> metric = SpecUtils.buildObject(TopNMetric.class, ms);
                if (metric != null) {
                    task.addMetric(metric);
                } else {
                    throw new RuntimeException("cannot build metric for " + ms.getJSON());
                }
            }
        }

        String sel = ets.getCandidateItems();
        if (sel == null) {
            task.candidateSelector = ItemSelector.nullSelector();
        } else {
            task.candidateSelector = ItemSelector.compileSelector(sel);
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
     * Get the list size to use.
     * @return The number of items to recommend per user.
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * Set the list size to use.
     * @param n The number of items to recommend per user.
     */
    public void setListSize(int n) {
        listSize = n;
    }

    /**
     * Get the list of prediction metrics.
     * @return The list of prediction metrics.  This list is live, not copied, so it can be modified or cleared.
     */
    public List<TopNMetric<?>> getTopNMetrics() {
        return topNMetrics;
    }

    /**
     * Get the list of all metrics.
     * @return A list containing all metrics used by this task.
     */
    public List<Metric<?>> getAllMetrics() {
        ImmutableList.Builder<Metric<?>> metrics = ImmutableList.builder();
        metrics.addAll(topNMetrics);
        return metrics.build();
    }

    /**
     * Add a prediction metric.
     * @param metric The metric to add.
     */
    public void addMetric(TopNMetric<?> metric) {
        topNMetrics.add(metric);
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
        for (TopNMetric<?> pm: getTopNMetrics()) {
            columns.addAll(pm.getColumnLabels());
        }
        return columns.build();
    }

    @Override
    public void start(ExperimentOutputLayout outputLayout) {
        experimentOutputLayout = outputLayout;
        if (outputFile == null) {
            return;
        }

        TableLayoutBuilder tlb = TableLayoutBuilder.copy(outputLayout.getConditionLayout());
        TableLayout layout = tlb.addColumn("User")
                                .addColumn("Rank")
                                .addColumn("Item")
                                .addColumn("Score")
                                .build();
        try {
            logger.info("writing recommendations to {}", outputFile);
            outputTable = CSVWriter.open(outputFile.toFile(), layout, CompressionMode.AUTO);
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
    public ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, Recommender rec) {
        Preconditions.checkState(experimentOutputLayout != null, "experiment not started");
        TableWriter tlb = experimentOutputLayout.prefixTable(outputTable, dataSet, algorithm);
        LongSet items = dataSet.getAllItems();
        ItemRecommender irec = rec.getItemRecommender();
        if (irec == null) {
            logger.warn("algorithm {} has no item recommender", algorithm);
            return null;
        }

        List<MetricContext<?>> contexts = new ArrayList<>(topNMetrics.size());
        for (TopNMetric<?> metric: topNMetrics) {
            contexts.add(MetricContext.create(metric, algorithm, dataSet, rec));
        }

        return new TopNConditionEvaluator(tlb, irec, contexts, items);
    }

    static class MetricContext<X> {
        final TopNMetric<X> metric;
        final X context;

        public MetricContext(TopNMetric<X> m, X ctx) {
            metric = m;
            context = ctx;
        }

        @Nonnull
        public MetricResult measureUser(TestUser user, ResultList recommendations) {
            return metric.measureUser(user, recommendations, context);
        }

        @Nonnull
        public MetricResult getAggregateMeasurements() {
            return metric.getAggregateMeasurements(context);
        }

        /**
         * Create a new metric context. Indirected through this method to help the type checker.
         */
        public static <X> MetricContext<X> create(TopNMetric<X> metric, AlgorithmInstance algorithm, DataSet dataSet, Recommender rec) {
            X ctx = metric.createContext(algorithm, dataSet, rec);
            return new MetricContext<>(metric, ctx);
        }
    }

    class TopNConditionEvaluator implements ConditionEvaluator {
        private final TableWriter writer;
        private final ItemRecommender recommender;
        private final UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        private final List<MetricContext<?>> predictMetricContexts;
        private final LongSet allItems;

        public TopNConditionEvaluator(TableWriter tw, ItemRecommender rec, List<MetricContext<?>> mcs, LongSet items) {
            writer = tw;
            recommender = rec;
            predictMetricContexts = mcs;
            allItems = items;
        }

        @Nonnull
        @Override
        public Map<String, Object> measureUser(TestUser testUser) {
            // FIXME Support item selectors
            LongSet candidates = candidateSelector.selectItems(allItems, testUser);
            ResultList results = recommender.recommendWithDetails(testUser.getUserId(), listSize, candidates, null);

            // Measure the user results
            Map<String,Object> row = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                row.putAll(mc.measureUser(testUser, results).getValues());
            }

            // Write all attempted predictions
            int rank = 0;
            for (Result rec: results) {
                try {
                    rank += 1;
                    if (writer != null) {
                        writer.writeRow(testUser.getUserId(), rank, rec.getId(), rec.getScore());
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
                results.putAll(mc.getAggregateMeasurements().getValues());
            }
            return results;
        }
    }
}
