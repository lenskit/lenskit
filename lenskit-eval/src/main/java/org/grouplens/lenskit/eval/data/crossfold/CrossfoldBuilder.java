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
import org.grouplens.lenskit.eval.EvalTask;
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.kohsuke.MetaInfServices;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;

/**
 * Builder for crossfold data sources (used to do cross-validation).
 * @author Michael Ekstrand
 * @since 0.10
 */
public class CrossfoldBuilder extends AbstractEvalTaskBuilder implements Builder<CrossfoldSplit> {
    private int folds = 5;
    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new CountPartition<Rating>(10);
    private DataSource source;
    private String name;
    private Set<EvalTask> dependency;
    private File cacheDirectory;
    private Function<DAOFactory,DAOFactory> wrapper;

    public CrossfoldBuilder() {
        super();
    }

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

    
    public CrossfoldBuilder setCacheDir(File file) {
        this.cacheDirectory = file;
        return this;
    }
    
    

    /**
     * Specify a function that will wrap the DAO factories created by the crossfold. Used
     * to e.g. associate a text index with the split rating data. Note that the
     * Groovy configuration subsystem allows you to use a closure for the function,
     * so you can do this:
     * {@code
     * wrapper {
     *     return new WrappedDao(it)
     * }
     * }
     * @param f The function to wrap DAOs.
     * @return The builder (for chaining)
     */
    public CrossfoldBuilder setWrapper(Function<DAOFactory,DAOFactory> f) {
        wrapper = f;
        return this;
    }

    public CrossfoldSplit build() {
        CrossfoldSplit split = new CrossfoldSplit(name, dependency, source, folds, new Holdout(order, partition),
                cacheDirectory, wrapper);
        return split;
    }

    @MetaInfServices
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
