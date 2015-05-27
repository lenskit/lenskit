package org.lenskit.eval.crossfold;

import org.grouplens.lenskit.data.event.Rating;

public final class CrossfoldMethods {
    private CrossfoldMethods() {}

    /**
     * Create a crossfold method that splits users into disjoint partitions.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionUsers(Order<Rating> order, PartitionAlgorithm<Rating> part) {
        return new UserPartitionCrossfoldMethod(order, part);
    }

    /**
     * Create a crossfold method that splits users into disjoint samples.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @param size The number of users per sample.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleUsers(Order<Rating> order, PartitionAlgorithm<Rating> part, int size) {
        return new UserSampleCrossfoldMethod(order, part, size);
    }

    /**
     * Create a crossfold method that partitions ratings into disjoint partitions.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionRatings() {
        return new RatingPartitionCrossfoldMethod();
    }
}
