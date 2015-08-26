/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.lenskit.eval.crossfold.*;
import org.lenskit.specs.eval.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @deprecated Use {@link Crossfolder}.
 */
@Deprecated
public class CrossfoldTask extends AbstractTask<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldTask.class);

    private Crossfolder crossfolder;

    private Order<Rating> order = new RandomOrder();
    private PartitionAlgorithm<Rating> partition = new HoldoutNPartition<Rating>(10);
    private CrossfoldMethod method = CrossfoldMethod.PARTITION_USERS;
    private int sampleSize = 1000;

    public CrossfoldTask() {
        this(null);
    }

    public CrossfoldTask(String n) {
        super(n);
        crossfolder = new Crossfolder(n);
        crossfolder.setSkipIfUpToDate(true);
    }

    /**
     * Set the number of partitions to generate.
     *
     * @param partition The number of paritions
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setPartitions(int partition) {
        crossfolder.setPartitionCount(partition);
        return this;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Set the sample size (# of users sampled per partition).  Only meaningful when the method is
     * {@link CrossfoldMethod#SAMPLE_USERS}.
     * @param n The number of users to sample for each partition.
     * @return The task (for chaining).
     */
    public CrossfoldTask setSampleSize(int n) {
        sampleSize = n;
        return this;
    }

    /**
     * Set the pattern for the training set files. The pattern should have a single format conversion
     * capable of taking an integer ('%s' or '%d') which will be replaced with the fold number.
     *
     * @param pat The training file name pattern.
     * @return The CrossfoldCommand object  (for chaining)
     * @see String#format(String, Object...)
     */
    public CrossfoldTask setTrain(String pat) {
        String dir = pat.replaceFirst("%d\\.?", "");
        logger.warn("setTrain deprecated; treating pattern '{}' as dir '{}'", pat, dir);
        crossfolder.setOutputDir(dir);
        if (pat.endsWith(".pack")) {
            crossfolder.setOutputFormat(OutputFormat.PACK);
        } else if (pat.endsWith(".csv.gz")) {
            crossfolder.setOutputFormat(OutputFormat.CSV_GZIP);
        } else if (pat.endsWith(".csv.xz")) {
            crossfolder.setOutputFormat(OutputFormat.CSV_XZ);
        }
        return this;
    }

    /**
     * Set the pattern for the test set files.
     *
     * @param pat The test file name pattern.
     * @return The CrossfoldCommand object  (for chaining)
     * @see #setTrain(String)
     */
    public CrossfoldTask setTest(String pat) {
        logger.warn("setTest now has no effect.");
        return this;
    }

    /**
     * Set the pattern for train-test spec files.
     *
     * @param pat The train-test spec file pattern.
     * @return The CrossfoldCommand object  (for chaining)
     * @see #setTrain(String)
     */
    public CrossfoldTask setSpec(String pat) {
        logger.warn("setSpec now has no effect.");
        return this;
    }

    /**
     * Set the order for the train-test splitting. To split a user's ratings, the ratings are
     * first ordered by this order, and then partitioned.
     *
     * @param o The sort order.
     * @return The CrossfoldCommand object  (for chaining)
     * @see RandomOrder
     * @see TimestampOrder
     * @see #setHoldoutFraction(double)
     * @see #setHoldout(int)
     */
    public CrossfoldTask setOrder(Order<Rating> o) {
        order = o;
        return this;
    }

    /**
     * Set holdout to a fixed number of items per user.  Only meaningful when the method is
     * {@link CrossfoldMethod#PARTITION_USERS}.
     *
     * @param n The number of items to hold out from each user's profile.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setHoldout(int n) {
        partition = new HoldoutNPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout from using the retain part to a fixed number of items.
     * Only meaningful when the method is
     * {@link CrossfoldMethod#PARTITION_USERS}.
     * 
     * @param n The number of items to train data set from each user's profile.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setRetain(int n) {
        partition = new RetainNPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout to a fraction of each user's profile.
     * Only meaningful when the method is
     * {@link CrossfoldMethod#PARTITION_USERS}.
     *
     * @param f The fraction of a user's ratings to hold out.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setHoldoutFraction(double f){
        partition = new FractionPartition<Rating>(f);
        return this;
    }
    
    /**
     * Set the input data source.
     *
     * @param source The data source to use.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setSource(DataSource source) {
        crossfolder.setSource(source);
        return this;
    }

    /**
     * Set the force running option of the command. The crossfold will be forced to
     * ran with the isForced set to true regardless of whether the partition files
     * are up to date.
     *
     * @param force The force to run option
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setForce(boolean force) {
        crossfolder.setSkipIfUpToDate(!force);
        return this;
    }

    /**
     * Configure whether it splits per-user or per-rating.
     *
     * @param splitUsers {@code true} to split by users ({@link CrossfoldMethod#PARTITION_USERS}),
     *                   {@code false} to split by rating ({@link CrossfoldMethod#PARTITION_RATINGS}).
     * @deprecated Use {@link #setMethod(CrossfoldMethod)} instead.
     */
    @Deprecated
    public void setSplitUsers(boolean splitUsers) {
        if (splitUsers) {
            setMethod(CrossfoldMethod.PARTITION_USERS);
        } else {
            setMethod(CrossfoldMethod.PARTITION_RATINGS);
        }
    }

    /**
     * Get the method to be used for crossfolding.
     * @return The configured crossfold method.
     */
    public CrossfoldMethod getMethod() {
        return method;
    }

    /**
     * Set the crossfold method.  The default is {@link CrossfoldMethod#PARTITION_USERS}.
     *
     * @param m The crossfold method to use.
     */
    public CrossfoldTask setMethod(CrossfoldMethod m) {
        method = m;
        return this;
    }

    /**
     * Configure whether the data sets created by the crossfold will have
     * caching turned on.
     *
     * @param on Whether the data sets returned should cache.
     * @return The command (for chaining)
     */
    public CrossfoldTask setCache(boolean on) {
        logger.warn("crossfold cache directive is now a no-op");
        return this;
    }

    /**
     * Configure whether the train-test data sets generated by this task will be isolated.  If yes,
     * then each data set will be in its own isolation group; otherwise, they will all be in the
     * default isolation group (the all-zero UUID).
     * @param on {@code true} to produce isolated data sets.
     * @return The task (for chaining).
     */
    public CrossfoldTask setIsolate(boolean on) {
        logger.warn("isolate is now a no-op");
        return this;
    }

    /**
     * Query whether this task will produce isolated data sets.
     * @return {@code true} if this task will produce isolated data sets.
     */
    public boolean getIsolate() {
        return false;
    }

    /**
     * Configure whether to include timestamps in the output file.
     * @param ts {@code true} to include timestamps (the default), {@code false} otherwise.
     * @return The task (for chaining).
     */
    public CrossfoldTask setWriteTimestamps(boolean ts) {
        crossfolder.setWriteTimestamps(ts);
        return this;
    }

    /**
     * Query whether timestamps will be written.
     * @return {@code true} if output will include timestamps.
     */
    public boolean getWriteTimestamps() {
        return crossfolder.getWriteTimestamps();
    }

    /**
     * Get the visible name of this crossfold split.
     *
     * @return The name of the crossfold split.
     */
    @Override
    public String getName() {
        return crossfolder.getName();
    }

    public String getTrainPattern() {
        return null;
    }

    public String getTestPattern() {
        return null;
    }

    /**
     * Get the data source backing this crossfold manager.
     *
     * @return The underlying data source.
     */
    public DataSource getSource() {
        return crossfolder.getSource();
    }

    /**
     * Get the number of folds.
     *
     * @return The number of folds in this crossfold.
     */
    public int getPartitionCount() {
        return crossfolder.getPartitionCount();
    }

    public Holdout getHoldout() {
        return new Holdout(order, partition);
    }

    public boolean getForce() {
        return !crossfolder.getSkipIfUpToDate() || getProject().getConfig().force();
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     *
     * @return The partition files stored as a list of TTDataSet
     */
    @Override
    public List<TTDataSet> perform() throws TaskExecutionException {
        if (getProject().getConfig().force()) {
            crossfolder.setSkipIfUpToDate(false);
        }
        switch (method) {
        case PARTITION_RATINGS:
            crossfolder.setMethod(SplitMethods.partitionRatings());
            break;
        case PARTITION_USERS:
            crossfolder.setMethod(SplitMethods.partitionUsers(order, partition));
            break;
        case SAMPLE_USERS:
            crossfolder.setMethod(SplitMethods.sampleUsers(order, partition, sampleSize));
        }
        crossfolder.execute();
        return crossfolder.getDataSets();
    }

    @Override
    public String toString() {
        return String.format("{CXManager %s}", crossfolder.getSource());
    }
}
