package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Throwables;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelectors;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Top-N metric that writes recommendations to a file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OutputTopNMetric extends AbstractMetric<OutputTopNMetric.Context, Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(OutputTopNMetric.class);

    private final ExperimentOutputLayout outputLayout;
    private final TableWriter tableWriter;

    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;

    public OutputTopNMetric(ExperimentOutputLayout layout, File file,
                            int listSize, ItemSelector candidates, ItemSelector exclude) throws IOException {
        super(Void.TYPE, Void.TYPE);
        outputLayout = layout;

        TableLayout recLayout = TableLayoutBuilder.copy(layout.getCommonLayout())
                                                  .addColumn("User")
                                                  .addColumn("Item")
                                                  .addColumn("Rank")
                                                  .addColumn("Score")
                                                  .build();
        tableWriter = CSVWriter.open(file, recLayout);

        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context(outputLayout.prefixTable(tableWriter, algo, ds));
    }

    @Override
    public Void doMeasureUser(TestUser user, Context context) {
        List<ScoredId> recs;
        recs = user.getRecommendations(listSize, candidates, exclude);
        logger.debug("outputting {} recommendations for user {}", recs.size(), user.getUserId());
        int counter = 1;
        for (ScoredId rec: CollectionUtils.fast(recs)) {
            try {
                context.writer.writeRow(user.getUserId(), rec.getId(),
                                        counter, rec.getScore());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        return null;
    }

    @Override
    protected Void getTypedResults(Context context) {
        return null;
    }

    @Override
    public void close() throws IOException {
        tableWriter.close();
    }

    public static class Context {
        private final TableWriter writer;

        Context(TableWriter tw) {
            writer = tw;
        }
    }

    public static class Factory extends MetricFactory {
        @Override
        public Metric createMetric(TrainTestEvalTask task) throws IOException {
            return new OutputTopNMetric(task.getOutputLayout(), task.getRecommendOutput(), -1,
                                        ItemSelectors.allItems(),
                                        ItemSelectors.trainingItems());
        }

        @Override
        public List<String> getColumnLabels() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getUserColumnLabels() {
            return Collections.emptyList();
        }
    }
}
