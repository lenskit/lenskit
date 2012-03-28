/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data.crossfold;

import com.google.common.base.Function;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.AbstractEvalTaskBuilder;
import org.grouplens.lenskit.eval.data.DataSource;

import javax.annotation.Nonnull;

/**
 * Builder for crossfold data sources (used to do cross-validation).
 * @author Michael Ekstrand
 * @since 0.10
 */
public class CrossfoldTaskBuilder extends AbstractEvalTaskBuilder<CrossfoldTask> {
    private int folds = 5;
    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new CountPartition<Rating>(10);
    private DataSource source;
    private String name;
    private String trainPattern;
    private String testPattern;
    private Function<DAOFactory,DAOFactory> wrapper;

    public CrossfoldTaskBuilder() {
        super();
    }

    public CrossfoldTaskBuilder(String name) {
        this.name = name;
    }

    /**
     * Set the number of partitions to generate.
     * @param n The number of partitions to generate.
     * @return The builder (for chaining)
     */
    public CrossfoldTaskBuilder setPartitions(int n) {
        folds = n;
        return this;
    }

    /**
     * Set the pattern for the training set files. The pattern should have a single format conversion
     * capable of taking an integer ('%s' or '%d') which will be replaced with the fold number.
     * @param pat The training file name pattern.
     * @return The builder (for chaining)
     * @see String#format(String, Object...)
     */
    public CrossfoldTaskBuilder setTrain(String pat) {
        trainPattern = pat;
        return this;
    }

    /**
     * Set the pattern for the test set files.
     * @param pat The test file name pattern.
     * @return The builder (for chaining)
     * @see #setTrain(String)
     */
    public CrossfoldTaskBuilder setTest(String pat) {
        testPattern = pat;
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
    public CrossfoldTaskBuilder setOrder(Order<Rating> o) {
        order = o;
        return this;
    }

    /**
     * Set holdout to a fixed number of items per user.
     * @param n The number of items to hold out from each user's profile.
     * @return The builder (for chaining)
     */
    public CrossfoldTaskBuilder setHoldout(int n) {
        partition = new CountPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout to a fraction of each user's profile.
     * @param f The fraction of a user's ratings to hold out.
     * @return The builder (for chaining)
     */
    public CrossfoldTaskBuilder setHoldout(double f) {
        partition = new FractionPartition<Rating>(f);
        return this;
    }

    /**
     * Set the input data source.
     * @param source The data source to use.
     * @return The builder (for chaining)
     */
    public CrossfoldTaskBuilder setSource(DataSource source) {
        this.source = source;
        return this;
    }

    /**
     * Set a wrapper function for the constructed data sources.
     * @param wrapFun The wrapper function.
     * @return The builder (for chaining).
     * @see org.grouplens.lenskit.eval.data.CSVDataSourceBuilder#setWrapper(Function)
     */
    public CrossfoldTaskBuilder setWrapper(Function<DAOFactory,DAOFactory> wrapFun) {
        wrapper = wrapFun;
        return this;
    }

    public CrossfoldTask build() {
        if (trainPattern == null) {
            trainPattern = name + ".train.%d.csv";
        }
        if (testPattern == null) {
            testPattern = name + ".test.%d.csv";
        }
        return new CrossfoldTask(name, dependencies, source, folds,
                                 new Holdout(order, partition),
                                 trainPattern, testPattern, wrapper);
    }
}
