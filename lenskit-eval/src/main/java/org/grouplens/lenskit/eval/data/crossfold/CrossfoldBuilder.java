package org.grouplens.lenskit.eval.data.crossfold;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.data.DataSource;

import javax.annotation.Nonnull;

/**
 * Builder for crossfold data sources (used to do cross-validation).
 * @author Michael Ekstrand
 * @since 0.10
 */
public class CrossfoldBuilder implements Builder<CrossfoldSplit> {
    private int folds = 5;
    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new CountPartition<Rating>(10);
    private DataSource source;
    private String name;

    public CrossfoldBuilder() {}

    public CrossfoldBuilder(String name) {
        this.name = name;
    }

    /**
     * Set the number of partitions to generate.
     * @param n The number of partitions to generate.
     * @return The builder (for chaining)
     */
    public CrossfoldBuilder setPartitions(int n) {
        folds = n;
        return this;
    }

    /**
     * Set the order for the train-test splitting. To split a user's ratings, the ratings are
     * first ordered by this order, and then partitioned.
     * @param o The sort order.
     * @return The builder (for chaining)
     * @see RandomOrder
     * @see TimestampOrder
     * @see #setHoldout(double)
     * @see #setHoldout(int)
     */
    public CrossfoldBuilder setOrder(Order o) {
        order = o;
        return this;
    }

    /**
     * Set holdout to a fixed number of items per user.
     * @param n The number of items to hold out from each user's profile.
     * @return The builder (for chaining)
     */
    public CrossfoldBuilder setHoldout(int n) {
        partition = new CountPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout to a fraction of each user's profile.
     * @param f The fraction of a user's ratings to hold out.
     * @return The builder (for chaining)
     */
    public CrossfoldBuilder setHoldout(double f) {
        partition = new FractionPartition<Rating>(f);
        return this;
    }

    /**
     * Set the input data source.
     * @param source The data source to use.
     * @return The builder (for chaining)
     */
    public CrossfoldBuilder setSource(DataSource source) {
        this.source = source;
        return this;
    }

    public CrossfoldSplit build() {
        return new CrossfoldSplit(name, source, folds, new Holdout(order, partition));
    }

    @ServiceProvider
    public static class Factory implements BuilderFactory<CrossfoldSplit> {
        @Override
        public String getName() {
            return "crossfold";
        }

        @Nonnull @Override
        public CrossfoldBuilder newBuilder(String arg) {
            return new CrossfoldBuilder(arg);
        }
    }
}
