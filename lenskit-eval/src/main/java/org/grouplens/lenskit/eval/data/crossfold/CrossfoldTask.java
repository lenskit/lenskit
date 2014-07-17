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

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.RatingWriter;
import org.grouplens.lenskit.eval.data.RatingWriters;
import org.grouplens.lenskit.eval.data.pack.PackedDataSourceBuilder;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CrossfoldTask extends AbstractTask<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldTask.class);

    private DataSource source;
    private int partitionCount = 5;
    private String trainFilePattern;
    private String testFilePattern;
    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new HoldoutNPartition<Rating>(10);
    private boolean isForced;
    private CrossfoldMethod method = CrossfoldMethod.PARTITION_USERS;
    private int sampleSize = 1000;
    private boolean isolate = false;
    private boolean writeTimestamps = true;

    public CrossfoldTask() {
        super(null);
    }

    public CrossfoldTask(String n) {
        super(n);
    }

    /**
     * Set the number of partitions to generate.
     *
     * @param partition The number of paritions
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldTask setPartitions(int partition) {
        partitionCount = partition;
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
        trainFilePattern = pat;
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
        testFilePattern = pat;
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
        this.source = source;
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
        isForced = force;
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
        isolate = on;
        return this;
    }

    /**
     * Query whether this task will produce isolated data sets.
     * @return {@code true} if this task will produce isolated data sets.
     */
    public boolean getIsolate() {
        return isolate;
    }

    /**
     * Configure whether to include timestamps in the output file.
     * @param pack {@code true} to include timestamps (the default), {@code false} otherwise.
     * @return The task (for chaining).
     */
    public CrossfoldTask setWriteTimestamps(boolean pack) {
        writeTimestamps = pack;
        return this;
    }

    /**
     * Query whether timestamps will be written.
     * @return {@code true} if output will include timestamps.
     */
    public boolean getWriteTimestamps() {
        return writeTimestamps;
    }

    /**
     * Get the visible name of this crossfold split.
     *
     * @return The name of the crossfold split.
     */
    @Override
    public String getName() {
        String name = super.getName();
        if (name == null) {
            name = source.getName();
        }
        return name;
    }

    public String getTrainPattern() {
        if (trainFilePattern != null) {
            return trainFilePattern;
        } else {
            StringBuilder sb = new StringBuilder();
            String dir = getProject().getConfig().getDataDir();
            if (dir == null) {
                dir = ".";
            }
            return sb.append(dir)
                     .append(File.separator)
                     .append(getName())
                     .append("-crossfold")
                     .append(File.separator)
                     .append("train.%d.csv")
                     .toString();
        }
    }

    public String getTestPattern() {
        if (testFilePattern != null) {
            return testFilePattern;
        } else {
            StringBuilder sb = new StringBuilder();
            String dir = getProject().getConfig().getDataDir();
            if (dir == null) {
                dir = ".";
            }
            return sb.append(dir)
                     .append(File.separator)
                     .append(getName())
                     .append("-crossfold")
                     .append(File.separator)
                     .append("test.%d.csv")
                     .toString();
        }
    }

    /**
     * Get the data source backing this crossfold manager.
     *
     * @return The underlying data source.
     */
    public DataSource getSource() {
        return source;
    }

    /**
     * Get the number of folds.
     *
     * @return The number of folds in this crossfold.
     */
    public int getPartitionCount() {
        return partitionCount;
    }

    public Holdout getHoldout() {
        return new Holdout(order, partition);
    }

    public boolean getForce() {
        return isForced || getProject().getConfig().force();
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     *
     * @return The partition files stored as a list of TTDataSet
     */
    @Override
    public List<TTDataSet> perform() throws TaskExecutionException {
        if (!getForce()) {
            UpToDateChecker check = new UpToDateChecker();
            check.addInput(source.lastModified());
            for (File f: getFiles(getTrainPattern())) {
                check.addOutput(f);
            }
            for (File f: getFiles(getTestPattern())) {
                check.addOutput(f);
            }
            if (check.isUpToDate()) {
                logger.info("crossfold {} up to date", getName());
                return getTTFiles();
            }
        }
        try {
            createTTFiles();
        } catch (IOException ex) {
            throw new TaskExecutionException("Error writing data sets", ex);
        }
        return getTTFiles();
    }

    /**
     * Get the list of files satisfying the specified name pattern
     *
     * @param pattern The file name pattern
     * @return The list of files
     */
    protected File[] getFiles(String pattern) {
        File[] files = new File[partitionCount];
        for (int i = 0; i < partitionCount; i++) {
            files[i] = new File(String.format(pattern, i));
        }
        return files;
    }

    /**
     * Write train-test split files
     *
     * @throws IOException if there is an error writing the files.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    protected void createTTFiles() throws IOException {
        File[] trainFiles = getFiles(getTrainPattern());
        File[] testFiles = getFiles(getTestPattern());
        RatingWriter[] trainWriters = new RatingWriter[partitionCount];
        RatingWriter[] testWriters = new RatingWriter[partitionCount];
        Closer closer = Closer.create();
        try {
            for (int i = 0; i < partitionCount; i++) {
                File train = trainFiles[i];
                File test = testFiles[i];
                trainWriters[i] = closer.register(makeWriter(train));
                testWriters[i] = closer.register(makeWriter(test));
            }
            switch (method) {
            case PARTITION_USERS:
            case SAMPLE_USERS:
                writeTTFilesByUsers(trainWriters, testWriters);
                break;
            case PARTITION_RATINGS:
                writeTTFilesByRatings(trainWriters, testWriters);
                break;
            }
        } catch (Throwable th) {
            throw closer.rethrow(th);
        } finally {
            closer.close();
        }
    }
    
    /**
     * Write the split files by Users from the DAO using specified holdout method
     * 
     *
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     * @throws org.grouplens.lenskit.eval.TaskExecutionException
     */
    protected void writeTTFilesByUsers(RatingWriter[] trainWriters, RatingWriter[] testWriters) throws TaskExecutionException {
        logger.info("splitting data source {} to {} partitions by users",
                    getName(), partitionCount);
        Long2IntMap splits = splitUsers(source.getUserDAO());
        Cursor<UserHistory<Event>> historyCursor = source.getUserEventDAO().streamEventsByUser();
        Holdout mode = this.getHoldout();
        try {
            for (UserHistory<Event> history : historyCursor) {
                int foldNum = splits.get(history.getUserId());
                // FIXME Use filtered streaming
                List<Rating> ratings = new ArrayList<Rating>(history.filter(Rating.class));
                final int n = ratings.size();

                for (int f = 0; f < partitionCount; f++) {
                    if (f == foldNum) {
                        final int p = mode.partition(ratings, getProject().getRandom());
                        for (int j = 0; j < p; j++) {
                            trainWriters[f].writeRating(ratings.get(j));
                        }
                        for (int j = p; j < n; j++) {
                            testWriters[f].writeRating(ratings.get(j));
                        }
                    } else {
                        for (Rating rating : CollectionUtils.fast(ratings)) {
                            trainWriters[f].writeRating(rating);
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new TaskExecutionException("Error writing to the train test files", e);
        } finally {
            historyCursor.close();
        }
    }
    
    /**
     * Write the split files by Ratings from the DAO
     * 
     *
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     * @throws org.grouplens.lenskit.eval.TaskExecutionException
     */
    protected void writeTTFilesByRatings(RatingWriter[] trainWriters, RatingWriter[] testWriters) throws TaskExecutionException {
        logger.info("splitting data source {} to {} partitions by ratings",
                    getName(), partitionCount);
        ArrayList<Rating> ratings = Cursors.makeList(source.getEventDAO().streamEvents(Rating.class));
        Collections.shuffle(ratings);
        try {
            final int n = ratings.size();
            for (int i = 0; i < n; i++) {
                for (int f = 0; f < partitionCount; f++) {
                    int foldNum = i % partitionCount;
                    if (f == foldNum) {
                        testWriters[f].writeRating(ratings.get(i));
                    } else {
                        trainWriters[f].writeRating(ratings.get(i));
                    }
                }
            }
        } catch (IOException e) {
            throw new TaskExecutionException("Error writing to the train test files", e);
        }
    }

    /**
     * Writing a rating event to the file using table writer
     *
     * @param writer The table writer to output the rating
     * @param rating The rating event to output
     * @throws IOException The writer IO error
     */
    protected void writeRating(TableWriter writer, Rating rating) throws IOException {
        Preference pref = rating.getPreference();
        writer.writeRow(Lists.newArrayList(
                Long.toString(rating.getUserId()),
                Long.toString(rating.getItemId()),
                (pref != null ? Double.toString(pref.getValue()) : "NaN"),
                Long.toString(rating.getTimestamp())
        ));
    }

    /**
     * Split users ids to n splits, where n is the partitionCount
     *
     * @param dao The DAO of the source file
     * @return a map of users to partition numbers. Users not in a partition will return -1.
     */
    protected Long2IntMap splitUsers(UserDAO dao) {
        Long2IntMap userMap = new Long2IntOpenHashMap();
        userMap.defaultReturnValue(-1);

        switch (method) {
        case PARTITION_USERS:
            partitionUsers(userMap, dao.getUserIds());
            break;
        case SAMPLE_USERS:
            sampleUsers(userMap, dao.getUserIds());
            break;
        default:
            throw new RuntimeException("why is splitUsers running for non-user method?");
        }

        return userMap;
    }

    private void sampleUsers(Long2IntMap userMap, LongSet users) {
        if (partitionCount * sampleSize > users.size()) {
            logger.warn("cannot make {} disjoint samples of {} from {} users, partitioning",
                        partitionCount, sampleSize, users.size());
            partitionUsers(userMap, users);
        } else {
            logger.info("Sampling {} users into {} disjoint samples of {}",
                        users.size(), partitionCount, sampleSize);
            long[] userArray = users.toLongArray();
            LongArrays.shuffle(userArray, getProject().getRandom());
            int i = 0;
            for (int p = 0; p < partitionCount; p++) {
                final int start = i;
                for (; i < userArray.length && i - start < sampleSize; i++) {
                    userMap.put(userArray[i], p);
                }
            }
        }
    }

    private void partitionUsers(Long2IntMap userMap, LongSet users) {
        logger.info("Splititng {} users into {} partitions", users.size(), partitionCount);
        long[] userArray = users.toLongArray();
        LongArrays.shuffle(userArray, getProject().getRandom());
        for (int i = 0; i < userArray.length; i++) {
            final long user = userArray[i];
            userMap.put(user, i % partitionCount);
        }
    }

    /**
     * Get the train-test splits as data sets.
     * 
     * @return The partition files stored as a list of TTDataSet
     */
    public List<TTDataSet> getTTFiles() {
        List<TTDataSet> dataSets = new ArrayList<TTDataSet>(partitionCount);
        File[] trainFiles = getFiles(getTrainPattern());
        File[] testFiles = getFiles(getTestPattern());
        for (int i = 0; i < partitionCount; i++) {
            GenericTTDataBuilder ttBuilder = new GenericTTDataBuilder(getName() + "." + i);
            if (isolate) {
                ttBuilder.setIsolationGroup(UUID.randomUUID());
            }

            dataSets.add(ttBuilder.setTest(makeDataSource(testFiles[i]))
                                  .setTrain(makeDataSource(trainFiles[i]))
                                  .setAttribute("DataSet", getName())
                                  .setAttribute("Partition", i)
                                  .build());
        }
        return dataSets;
    }

    protected RatingWriter makeWriter(File file) throws IOException {
        Files.createParentDirs(file);
        if (Files.getFileExtension(file.getName()).equals("pack")) {
            EnumSet<BinaryFormatFlag> flags = BinaryFormatFlag.makeSet();
            if (writeTimestamps) {
                flags.add(BinaryFormatFlag.TIMESTAMPS);
            }
            return RatingWriters.packed(file, flags);
        } else {
            return RatingWriters.csv(file, writeTimestamps);
        }
    }

    protected DataSource makeDataSource(File file) {
        if (Files.getFileExtension(file.getName()).equals("pack")) {
            return new PackedDataSourceBuilder()
                    .setDomain(source.getPreferenceDomain())
                    .setFile(file)
                    .build();
        } else {
            return new CSVDataSourceBuilder()
                    .setDomain(source.getPreferenceDomain())
                    .setFile(file)
                    .build();
        }
    }

    @Override
    public String toString() {
        return String.format("{CXManager %s}", source);
    }
}
