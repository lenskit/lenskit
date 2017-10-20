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
package org.lenskit.eval.crossfold;

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;

public final class CrossfoldMethods {
    private CrossfoldMethods() {}

    /**
     * Create a crossfold method that splits users into disjoint partitions.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionUsers(SortOrder order, HistoryPartitionMethod part) {
        return new GroupedCrossfoldMethod(CommonTypes.USER, CommonAttributes.USER_ID,
                                          GroupEntitySplitter.partition(),
                                          order, part);
    }

    /**
     * Create a crossfold method that splits users into disjoint samples.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @param size The number of users per sample.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleUsers(SortOrder order, HistoryPartitionMethod part, int size) {
        return new GroupedCrossfoldMethod(CommonTypes.USER, CommonAttributes.USER_ID,
                                          GroupEntitySplitter.disjointSample(size),
                                          order, part);
    }

    /**
     * Create a crossfold method that partitions ratings into disjoint partitions.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionEntities() {
        return new EntityPartitionCrossfoldMethod();
    }

    /**
     * Create a crossfold method that creates disjoint samples of entities into disjoint partitions.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleEntities(int size) {
        return new EntitySampleCrossfoldMethod(size);
    }

    /**
     * Create a crossfold method that splits items into disjoint partitions.
     * @param part the partition algorithm for item ratings.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionItems(HistoryPartitionMethod part) {
        return new GroupedCrossfoldMethod(CommonTypes.ITEM, CommonAttributes.ITEM_ID,
                                          GroupEntitySplitter.partition(),
                                          SortOrder.RANDOM, part);
    }

    /**
     * Create a crossfold method that splits items into disjoint samples.
     * @param part the partition algorithm for item ratings.
     * @param size The number of items per sample.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleItems(HistoryPartitionMethod part, int size) {
        return new GroupedCrossfoldMethod(CommonTypes.ITEM, CommonAttributes.ITEM_ID,
                                          GroupEntitySplitter.disjointSample(size),
                                          SortOrder.RANDOM, part);
    }
}
