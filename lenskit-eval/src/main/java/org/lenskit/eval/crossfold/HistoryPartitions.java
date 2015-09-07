package org.lenskit.eval.crossfold;

/**
 * Factories for creating {@link HistoryPartitionMethod} instances.
 */
public final class HistoryPartitions {
    private HistoryPartitions() {}

    /**
     * Hold out a fixed number of ratings.
     */
    public static HistoryPartitionMethod holdout(int n) {
        return new HoldoutNHistoryPartitionMethod(n);
    }

    /**
     * Hold out a fraction of ratings.
     */
    public static HistoryPartitionMethod holdoutFraction(double f) {
        return new FractionHistoryPartitionMethod(f);
    }

    /**
     * Retain a fixed number of ratings.
     */
    public static HistoryPartitionMethod retain(int n) {
        return new RetainNHistoryPartitionMethod(n);
    }
}
