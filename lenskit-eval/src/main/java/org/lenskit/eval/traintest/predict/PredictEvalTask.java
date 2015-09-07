package org.lenskit.eval.traintest.predict;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.events.Event;
import org.lenskit.eval.traintest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An eval task that attempts to predict the user's test ratings.
 */
public class PredictEvalTask implements EvalTask {
    private static final Logger logger = LoggerFactory.getLogger(PredictEvalTask.class);
    private ExperimentOutputLayout experimentOutputLayout;
    private Path outputFile;
    private TableWriter outputTable;

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

    @Override
    public Set<String> getGlobalColumns() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getUserColumns() {
        return Collections.emptySet();
    }

    @Override
    public void start(ExperimentOutputLayout outputLayout) {
        experimentOutputLayout = outputLayout;
        if (outputFile == null) {
            return;
        }

        TableLayoutBuilder tlb = TableLayoutBuilder.copy(outputLayout.getConditionLayout());
        TableLayout layout = tlb.addColumn("User")
                                .addColumn("Item")
                                .addColumn("Rating")
                                .addColumn("Prediction")
                                .build();
        try {
            logger.info("writing predictions to {}", outputFile);
            outputTable = CSVWriter.open(outputFile.toFile(), layout, CompressionMode.AUTO);
        } catch (IOException e) {
            throw new EvaluationException("error opening prediction output file", e);
        }
    }

    @Override
    public void finish() {
        experimentOutputLayout = null;
        try {
            outputTable.close();
            outputTable = null;
        } catch (IOException e) {
            throw new EvaluationException("error closing prediction output file", e);
        }
    }

    @Override
    public ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, Recommender rec) {
        Preconditions.checkState(experimentOutputLayout != null, "experiment not started");
        TableWriter tlb = experimentOutputLayout.prefixTable(outputTable, dataSet, algorithm);
        RatingPredictor pred = rec.getRatingPredictor();
        if (pred == null) {
            logger.warn("algorithm {} has no rating predictor", algorithm);
            return null;
        }

        return new PredictConditionEvaluator(tlb, pred);
    }

    class PredictConditionEvaluator implements ConditionEvaluator {
        private final TableWriter writer;
        private final RatingPredictor predictor;
        private final UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();

        public PredictConditionEvaluator(TableWriter tw, RatingPredictor pred) {
            writer = tw;
            predictor = pred;
        }

        @Nonnull
        @Override
        public Map<String, Object> measureUser(UserHistory<Event> testUser) {
            SparseVector vector = summarizer.summarize(testUser);
            ResultMap results = predictor.predictWithDetails(testUser.getUserId(), vector.keySet());

            for (VectorEntry e: vector) {
                Result pred = results.get(e.getKey());
                try {
                    if (writer != null) {
                        writer.writeRow(testUser.getUserId(), e.getKey(), e.getValue(),
                                        pred != null ? pred.getScore() : null);
                    }
                } catch (IOException ex) {
                    throw new EvaluationException("error writing prediction row", ex);
                }
            }

            return Collections.emptyMap();
        }

        @Nonnull
        @Override
        public Map<String, Object> finish() {
            return Collections.emptyMap();
        }
    }
}
