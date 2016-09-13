package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.lenskit.api.Recommender;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * reference for this metrics is:
 * https://pdfs.semanticscholar.org/08ba/daad9669b69b16ce9437d0b2d52b5f33c8dd.pdf
 */
public class TopNBPREFMetric extends ListOnlyTopNMetric<MeanAccumulator> {
    private static final Logger logger = LoggerFactory.getLogger(TopNNDCGMetric.class);
    public static final String DEFAULT_COLUMN = "TopN.BPREF";
    private final String columnName;

    protected TopNBPREFMetric(List<String> labels, List<String> aggLabels, String columnName) {
        super(labels, aggLabels);
        this.columnName = columnName;
    }

    /**
     * Construct a TopN BPREF metric from a spec.
     * @param spec The spec.
     */
    @JsonCreator
    public TopNBPREFMetric(Spec spec) {
        this(spec.getColumnName());
    }

    /**
     * Construct a new TopN BPREF metric.
     * @param name The column name to use.
     */
    public TopNBPREFMetric(String name) {
        super(Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)),
              Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)));
        columnName = StringUtils.defaultString(name, DEFAULT_COLUMN);
    }

    @Nullable
    @Override
    public MeanAccumulator createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        return new MeanAccumulator();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(MeanAccumulator context) {
        return MetricResult.singleton(columnName, context.getMean());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, int targetLength, LongList recommendations, MeanAccumulator context) {
        if (recommendations == null) {
            return MetricResult.empty();
        }

        LongSet relevantItems = user.getTestItems();
        long[] rArray = relevantItems.toLongArray();
        long[] rList = recommendations.toLongArray();

        double bpref = computeBPREF(rArray, rList);

        context.add(bpref);
        return MetricResult.singleton(columnName, bpref);
    }

    /**
     * The formula to measure for BPREF is:
     * For each r of R sumOf(1 - (Nr / R))
     * BPREF = (1 / R) * sum
     * Where R = Total relevant Items.
     * r = A relevant item
     * Nr = number of non relevant items appear before a relevant item in the recommendation list
     */
    double computeBPREF(long[] rArray, long[] recommendations) {
        //count of relevant items (R).
        double rItemCount = rArray.length;

        //count of non relevant items found in the recommendation list (Nr).
        int nrItemCount = 0;

        //sum
        double total = 0;

        for (long item : recommendations) {
            if (!rArray.equals(item)) {
                nrItemCount++;
            }
            else {
                total = total + (1 - (nrItemCount / rItemCount));
            }
        }

        double bpref = (1 / rItemCount) * total;

        return bpref;
    }

    /**
     * Specification for configuring BPREF metrics.
     */
    @JsonIgnoreProperties("type")
    public static class Spec {
        private String name;

        public String getColumnName() {
            return name;
        }

        public void setColumnName(String name) {
            this.name = name;
        }
    }
}
