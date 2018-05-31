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
package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.api.*;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.eval.traintest.*;
import org.lenskit.eval.traintest.metrics.Metric;
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.predict.PredictEvalTask;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.keys.LongSortedArraySet;
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
 * An eval task that attempts to recommend items for a test user.
 */
public class RecommendEvalTask implements EvalTask {
    private static final Logger logger = LoggerFactory.getLogger(RecommendEvalTask.class);
    private static final TopNMetric<?>[] DEFAULT_METRICS = {
            new TopNLengthMetric(),
            new TopNNDCGMetric()
    };

    private Path outputFile;
    private Path itemOutputFile;
    private String labelPrefix;
    private int listSize = -1;
    private boolean separateItems;
    private List<TopNMetric<?>> topNMetrics = Lists.newArrayList(DEFAULT_METRICS);
    private volatile ItemSelector candidateSelector = ItemSelector.allItems();
    private volatile ItemSelector excludeSelector = ItemSelector.userTrainItems();

    private ExperimentOutputLayout experimentOutputLayout;
    private TableWriter outputTable;
    private TableWriter itemOutputTable;
    private TableLayout itemOutputLayout;

    /**
     * Create a new recommend eval task.
     */
    public RecommendEvalTask() {}

    /**
     * Parse a recommend task from JSON.
     * @param json The JSON data.
     * @param base The base URI (for resolving relative paths).
     * @return The task.
     * @throws IOException If there is an I/O error.
     */
    public static RecommendEvalTask fromJSON(JsonNode json, URI base) throws IOException {
        RecommendEvalTask task = new RecommendEvalTask();

        String outFile = json.path("output_file").asText(null);
        if (outFile != null) {
            task.setOutputFile(Paths.get(base.resolve(outFile)));
        }

        String itemOut = json.path("item_output_file").asText(null);
        if (itemOut != null) {
            task.setItemOutputFile(Paths.get(base.resolve(itemOut)));
        }

        task.setSeparateItems(json.path("separate_items").asBoolean(false));

        task.setLabelPrefix(json.path("label_prefix").asText(null));
        task.setListSize(json.path("list_size").asInt(-1));

        String sel = json.path("candidates").asText(null);
        if (sel != null) {
            task.setCandidateSelector(ItemSelector.compileSelector(sel));
        }
        sel = json.path("exclude").asText(null);
        if (sel != null) {
            task.setExcludeSelector(ItemSelector.compileSelector(sel));
        }

        JsonNode metrics = json.get("metrics");
        if (metrics != null && !metrics.isNull()) {
            task.topNMetrics.clear();
            MetricLoaderHelper mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(PredictEvalTask.class),
                                                            "topn-metrics");
            for (JsonNode mn: metrics) {
                TopNMetric<?> metric = mlh.createMetric(TopNMetric.class, mn);
                if (metric != null) {
                    task.addMetric(metric);
                } else {
                    throw new VerifyError("cannot build metric for " + mn.toString());
                }
            }
        }

        return task;
    }

    /**
     * Get the output file for writing recommendations.
     * @return The output file, or {@code null} if no file is configured.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * Set the output file for recommendations.
     * @param file The output file for writing predictions. Will get a CSV file.
     */
    public void setOutputFile(Path file) {
        outputFile = file;
    }

    /**
     * Get the output file for writing per-target-item results.
     * @return The output file, or {@code null} if no file is configured.
     */
    public Path getItemOutputFile() {
        return itemOutputFile;
    }

    /**
     * Set the output file for per-target-item results.
     * @param file The output file for writing predictions. Will get a CSV file.
     */
    public void setItemOutputFile(Path file) {
        itemOutputFile = file;
    }

    /**
     * Get the prefix applied to column labels.
     * @return The column label prefix.
     */
    public String getLabelPrefix() {
        return labelPrefix;
    }

    /**
     * Set the prefix applied to column labels.  If provided, it will be prepended to column labels from this task,
     * along with a ".".
     * @param prefix The label prefix.
     */
    public void setLabelPrefix(String prefix) {
        labelPrefix = prefix;
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
     * Query whether this task will separate items.
     * @return {@code true} if test items are evaluated separately.
     */
    public boolean getSeparateItems() {
        return separateItems;
    }

    /**
     * Control whether this task will separate items.
     * @param sep {@code true} to evaluate test items separately.
     */
    public void setSeparateItems(boolean sep) {
        separateItems = sep;
    }

    /**
     * Get the active candidate selector.
     * @return The candidate selector to use.
     */
    public ItemSelector getCandidateSelector() {
        return candidateSelector;
    }

    /**
     * Set the candidate selector.
     * @param sel The candidate selector.
     */
    public void setCandidateSelector(ItemSelector sel) {
        candidateSelector = sel;
    }

    /**
     * Get the active exclude selector.
     * @return The exclude selector to use.
     */
    public ItemSelector getExcludeSelector() {
        return excludeSelector;
    }

    /**
     * Set the exclude selector.
     * @param sel The exclude selector.
     */
    public void setExcludeSelector(ItemSelector sel) {
        excludeSelector = sel;
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
            for (String label: m.getAggregateColumnLabels()) {
                columns.add(prefixColumn(label));
            }
        }
        return columns.build();
    }

    @Override
    public List<String> getUserColumns() {
        if (separateItems) {
            Collections.emptyList();
        }

        ImmutableList.Builder<String> columns = ImmutableList.builder();
        for (TopNMetric<?> pm: getTopNMetrics()) {
            for (String label: pm.getColumnLabels()) {
                columns.add(prefixColumn(label));
            }
        }
        return columns.build();
    }

    private String prefixColumn(String input) {
        String pfx = getLabelPrefix();
        if (pfx == null) {
            return input;
        } else {
            return pfx + "." + input;
        }
    }

    @Override
    public void start(ExperimentOutputLayout outputLayout) {
        experimentOutputLayout = outputLayout;
        Path outFile = getOutputFile();
        if (outFile != null) {
            logger.info("setting up recommendation output to {}", outFile);
            TableLayoutBuilder tlb = TableLayoutBuilder.copy(outputLayout.getConditionLayout());
            tlb.addColumn("User");
            if (separateItems) {
                tlb.addColumn("TargetItem");
            }

            TableLayout layout = tlb.addColumn("Rank")
                                    .addColumn("Item")
                                    .addColumn("Score")
                                    .build();
            try {
                logger.info("writing recommendations to {}", outFile);
                outputTable = CSVWriter.open(outFile.toFile(), layout, CompressionMode.AUTO);
            } catch (IOException e) {
                throw new EvaluationException("error opening recommendation output file", e);
            }
        }

        Path itemOut = getItemOutputFile();
        if (itemOut != null && separateItems) {
            logger.info("setting up per-item output to {}", itemOut);
            TableLayoutBuilder tlb = TableLayoutBuilder.copy(outputLayout.getConditionLayout());
            tlb.addColumn("User")
               .addColumn("TargetItem");
            for (TopNMetric<?> pm: getTopNMetrics()) {
                for (String label: pm.getColumnLabels()) {
                    tlb.addColumn(label);
                }
            }

            itemOutputLayout = tlb.build();

            try {
                logger.info("writing per-item results to {}", outFile);
                itemOutputTable = CSVWriter.open(itemOut.toFile(), itemOutputLayout, CompressionMode.AUTO);
            } catch (IOException e) {
                throw new EvaluationException("error opening per-item result file", e);
            }
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
                throw new EvaluationException("error closing recommendation output file", e);
            }
        }

        if (itemOutputTable != null) {
            try {
                itemOutputTable.close();
                itemOutputTable = null;
            } catch (IOException e) {
                throw new EvaluationException("error closing per-item recommendation output file", e);
            }
        }
    }

    @Override
    public ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine rec) {
        Preconditions.checkState(experimentOutputLayout != null, "experiment not started");
        TableWriter recTable = experimentOutputLayout.prefixTable(outputTable, dataSet, algorithm);
        LongSortedArraySet items = LongUtils.packedSet(dataSet.getAllItems());

        // we need details to write recommendation output
        boolean useDetails = recTable != null;
        List<MetricContext<?>> contexts = new ArrayList<>(topNMetrics.size());
        for (TopNMetric<?> metric: topNMetrics) {
            logger.debug("setting up metric {}", metric);
            MetricContext<?> mc = MetricContext.create(metric, algorithm, dataSet, rec);
            contexts.add(mc);
            // does this metric require details?
            useDetails |= mc.usesDetails();
        }

        if (separateItems) {
            TableWriter itemTable = experimentOutputLayout.prefixTable(itemOutputTable, dataSet, algorithm);
            return new SeparateTopNConditionEvaluator(recTable, itemTable, contexts, items, useDetails);
        } else {
            assert itemOutputTable == null;
            return new BatchedTopNConditionEvaluator(recTable, contexts, items, useDetails);
        }
    }

    static class MetricContext<X> {
        final TopNMetric<X> metric;
        final X context;

        public MetricContext(TopNMetric<X> m, X ctx) {
            metric = m;
            context = ctx;
        }

        public boolean usesDetails() {
            return !(metric instanceof ListOnlyTopNMetric);
        }

        @Nonnull
        public MetricResult measureUser(Recommender rec, TestUser user, int n, ResultList recommendations) {
            return metric.measureUser(rec, user, n, recommendations, context);
        }

        @Nonnull
        public MetricResult measureUser(Recommender rec, TestUser user, int n, LongList recommendations) {
            return ((ListOnlyTopNMetric<X>) metric).measureUserRecList(rec, user, n, recommendations, context);
        }

        @Nonnull
        public MetricResult getAggregateMeasurements() {
            return metric.getAggregateMeasurements(context);
        }

        /**
         * Create a new metric context. Indirected through this method to help the type checker.
         */
        public static <X> MetricContext<X> create(TopNMetric<X> metric, AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
            X ctx = metric.createContext(algorithm, dataSet, engine);
            return new MetricContext<>(metric, ctx);
        }
    }

    class BatchedTopNConditionEvaluator implements ConditionEvaluator {
        private final TableWriter writer;
        private final List<MetricContext<?>> predictMetricContexts;
        private final LongSortedArraySet allItems;
        private final boolean useDetails;

        public BatchedTopNConditionEvaluator(TableWriter tw,
                                             List<MetricContext<?>> mcs, LongSortedArraySet items, boolean details) {
            writer = tw;
            predictMetricContexts = mcs;
            allItems = items;
            useDetails = details;
        }

        @Nonnull
        @Override
        public Map<String, Object> measureUser(Recommender recommender, TestUser testUser) {
            ItemRecommender irec = recommender.getItemRecommender();
            if (irec == null) {
                logger.debug("recommender cannot produce recommendations");
                return Collections.emptyMap();
            }

            LongSet candidates = getCandidateSelector().selectItems(allItems, recommender, testUser);
            LongSet excludes = getExcludeSelector().selectItems(allItems, recommender, testUser);
            int n = getListSize();
            ResultList results = null;
            LongList items = null;
            if (useDetails) {
                logger.debug("generating {} detailed recommendations for user {}", n, testUser.getUser());
                results = irec.recommendWithDetails(testUser.getUserId(), n,
                                                    candidates, excludes);
            } else {
                // no one needs details, save time collecting them
                logger.debug("generating {} recommendations for user {}", n, testUser.getUser());
                items = LongUtils.asLongList(irec.recommend(testUser.getUserId(), n,
                                                            candidates, excludes));
            }

            // Measure the user results
            Map<String,Object> row = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                MetricResult res;
                if (useDetails) {
                    res = mc.measureUser(recommender, testUser, n, results);
                } else {
                    res = mc.measureUser(recommender, testUser, n, items);
                }
                row.putAll(res.withPrefix(getLabelPrefix())
                              .getValues());
            }

            writeRecommendations(testUser, results);

            return row;
        }

        private void writeRecommendations(TestUser testUser, ResultList results) {
            assert writer == null || results != null;
            if (writer == null) {
                return;
            }

            int rank = 0;
            for (Result rec : results) {
                try {
                    rank += 1;
                    writer.writeRow(testUser.getUserId(), rank, rec.getId(), rec.getScore());
                } catch (IOException ex) {
                    throw new EvaluationException("error writing prediction row", ex);
                }
            }
        }

        @Nonnull
        @Override
        public Map<String, Object> finish() {
            Map<String,Object> results = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                logger.debug("finishing metric {}", mc.metric);
                results.putAll(mc.getAggregateMeasurements()
                                 .withPrefix(getLabelPrefix())
                                 .getValues());
            }
            return results;
        }
    }

    class SeparateTopNConditionEvaluator implements ConditionEvaluator {
        private final TableWriter writer;
        private final TableWriter itemWriter;
        private final List<MetricContext<?>> predictMetricContexts;
        private final LongSortedArraySet allItems;
        private final boolean useDetails;

        public SeparateTopNConditionEvaluator(@Nullable TableWriter tw, @Nullable TableWriter itw,
                                              List<MetricContext<?>> mcs, LongSortedArraySet items, boolean details) {
            writer = tw;
            itemWriter = itw;
            predictMetricContexts = mcs;
            allItems = items;
            useDetails = details;
        }

        @Nonnull
        @Override
        public Map<String, Object> measureUser(Recommender recommender, TestUser testUser) {
            ItemRecommender itemRecommender = recommender.getItemRecommender();
            if (itemRecommender == null) {
                logger.debug("recommender cannot produce recommendations");
                return Collections.emptyMap();
            }

            List<Entity> history = testUser.getTestHistory();
            logger.debug("analyzing for user {} with {} test items", testUser.getUserId(), history.size());
            for (Entity te: history) {
                TestUserBuilder tub = new TestUserBuilder();
                tub.setUserId(testUser.getUserId())
                   .setTrainHistory(testUser.getTrainHistory())
                   .setTestHistory(Lists.newArrayList(te));
                TestUser tu2 = tub.build();

                LongSet candidates = getCandidateSelector().selectItems(allItems, recommender, tu2);
                LongSet excludes = getExcludeSelector().selectItems(allItems, recommender, tu2);
                int n = getListSize();
                ResultList results = null;
                LongList items = null;
                logger.debug("generating recommendations for user {}, item {}",
                             testUser.getUserId(), te.maybeGet(CommonAttributes.ITEM_ID));
                if (useDetails) {
                    results = itemRecommender.recommendWithDetails(tu2.getUserId(), n,
                                                                   candidates, excludes);
                } else {
                    // no one needs details, save time collecting them
                    items = LongUtils.asLongList(itemRecommender.recommend(tu2.getUserId(), n,
                                                                           candidates, excludes));
                }

                // Measure the user results for this item
                TableLayout il = itemWriter != null ? itemWriter.getLayout() : null;
                ArrayList<Object> row = new ArrayList<>();
                row.add(tu2.getUserId());
                row.add(te.getLong(CommonAttributes.ITEM_ID));
                while (il != null && row.size() < il.getColumnCount()) {
                    row.add(null);
                }
                for (MetricContext<?> mc: predictMetricContexts) {
                    MetricResult res;
                    if (useDetails) {
                        res = mc.measureUser(recommender, tu2, n, results);
                    } else {
                        res = mc.measureUser(recommender, tu2, n, items);
                    }
                    if (il != null) {
                        for (Map.Entry<String, Object> rv : res.getValues().entrySet()) {
                            int idx = il.columnIndex(rv.getKey());
                            row.set(idx, rv.getValue());
                        }
                    }
                }

                if (itemWriter != null) {
                    try {
                        itemWriter.writeRow(row);
                    } catch (IOException e) {
                        throw new EvaluationException("error writing target item result row", e);
                    }
                }

                writeRecommendations(tu2, te.getLong(CommonAttributes.ITEM_ID), results);
            }
            return Collections.emptyMap();
        }

        private void writeRecommendations(TestUser user, long item, ResultList results) {
            assert writer == null || results != null;
            if (writer == null) {
                return;
            }
            int rank = 0;
            for (Result rec : results) {
                try {
                    rank += 1;
                    writer.writeRow(user.getUserId(), item,
                                    rank, rec.getId(), rec.getScore());
                } catch (IOException ex) {
                    throw new EvaluationException("error writing prediction row", ex);
                }
            }
        }

        @Nonnull
        @Override
        public Map<String, Object> finish() {
            Map<String,Object> results = new HashMap<>();
            for (MetricContext<?> mc: predictMetricContexts) {
                logger.debug("finishing metric {}", mc.metric);
                results.putAll(mc.getAggregateMeasurements()
                                 .withPrefix(getLabelPrefix())
                                 .getValues());
            }
            return results;
        }
    }
}
