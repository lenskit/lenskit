/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Function;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.data.CSVDataSourceCommand;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataCommand;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class CrossfoldCommand extends AbstractCommand<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldCommand.class);

    private static final Random random = new Random();

    private DataSource source;
    private int partitionCount = 5;
    @Nullable
    private String trainFilePattern;
    @Nullable
    private String testFilePattern;

    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new HoldoutNPartition<Rating>(10);
    private boolean isForced;
    private boolean splitUsers = true;

    @Nullable
    private Function<DAOFactory, DAOFactory> wrapper;


    public CrossfoldCommand() {
        this(null);
    }

    public CrossfoldCommand(String n) {
        super(n);
    }

    /**
     * Set the number of partitions to generate.
     *
     * @param partition The number of paritions
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setPartitions(int partition) {
        partitionCount = partition;
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
    public CrossfoldCommand setTrain(String pat) {
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
    public CrossfoldCommand setTest(String pat) {
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
     * @see #setHoldout(double)
     * @see #setHoldout(int)
     */
    public CrossfoldCommand setOrder(Order<Rating> o) {
        order = o;
        return this;
    }

    /**
     * Set holdout to a fixed number of items per user.
     *
     * @param n The number of items to hold out from each user's profile.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setHoldout(int n) {
        partition = new HoldoutNPartition<Rating>(n);
        return this;
    }

    /**
     * @deprecated use {@link #setHoldoutFraction(double)} instead.
     */
    @Deprecated
    public CrossfoldCommand setHoldout(double f) {
        partition = new FractionPartition<Rating>(f);
        return this;
    }
    
    /**
     * Set holdout from using the retain part to a fixed number of items.
     * 
     * @param n The number of items to train data set from each user's profile.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setRetain(int n) {
        partition = new RetainNPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout to a fraction of each user's profile.
     *
     * @param f The fraction of a user's ratings to hold out.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setHoldoutFraction(double f){
        partition = new FractionPartition<Rating>(f);
        return this;
    }
    
    /**
     * Set the input data source.
     *
     * @param source The data source to use.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setSource(DataSource source) {
        this.source = source;
        return this;
    }

    /**
     * Set a wrapper function for the constructed data sources.
     *
     * @param wrapFun The wrapper function.
     * @return The CrossfoldCommand object  (for chaining)
     * @see org.grouplens.lenskit.eval.data.CSVDataSourceCommand#setWrapper(Function)
     */
    public CrossfoldCommand setWrapper(Function<DAOFactory, DAOFactory> wrapFun) {
        wrapper = wrapFun;
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
    public CrossfoldCommand setForce(boolean force) {
        isForced = force;
        return this;
    }

    public void setSplitUsers(boolean splitUsers) {
        this.splitUsers = splitUsers;
    }

    /**
     * Get the visible name of this crossfold split.
     *
     * @return The name of the crossfold split.
     */
    @Override
    public String getName() {
        if (name == null) {
            return source.getName();
        } else {
            return name;
        }
    }

    @Nonnull
    public String getTrainPattern() {
        if (trainFilePattern == null) {
            String path = new File(getConfig().getDataDir(), getName()).getPath();
            return path + ".train.%d.csv";
        } else {
            return trainFilePattern;
        }
    }

    @Nonnull
    public String getTestPattern() {
        if (testFilePattern == null) {
            String path = new File(getConfig().getDataDir(), getName()).getPath();
            return path + ".test.%d.csv";
        } else {
            return testFilePattern;
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
        return isForced || getConfig().force();
    }

    public boolean getSplitUsers() {
        return splitUsers;
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     *
     * @return List<TTDataSet> The partition files stored as a list of TTDataSet
     * @throws org.grouplens.lenskit.eval.CommandException
     *
     */
    @Override
    public List<TTDataSet> call() throws CommandException {
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
        createTTFiles();
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
     * @throws org.grouplens.lenskit.eval.CommandException
     *          Any error
     */
    protected void createTTFiles() throws CommandException {
        File[] trainFiles = getFiles(getTrainPattern());
        File[] testFiles = getFiles(getTestPattern());
        TableWriter[] trainWriters = new TableWriter[partitionCount];
        TableWriter[] testWriters = new TableWriter[partitionCount];
        try {
            for (int i = 0; i < partitionCount; i++) {
                File train = trainFiles[i];
                File test = testFiles[i];
                try {
                    trainWriters[i] = CSVWriter.open(train, null);
                    testWriters[i] = CSVWriter.open(test, null);
                } catch (IOException e) {
                    throw new CommandException("Error creating train test file writer", e);
                }
            }
            DAOFactory factory = source.getDAOFactory();
            DataAccessObject daoSnap = factory.snapshot();
            try {
                if (getSplitUsers()) {                   
                    writeTTFilesByUsers(trainWriters, testWriters, daoSnap);
                } else {                    
                    writeTTFilesByRatings(trainWriters, testWriters, daoSnap);
                }
            } finally {
                daoSnap.close();
            }
        } finally {
            LKFileUtils.close(logger, trainWriters);
            LKFileUtils.close(logger, testWriters);
        }
    }
    
    /**
     * Write the split files by Users from the DAO using specified holdout method
     * 
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     * @param dao The DAO of the data source file
     * @throws CommandException
     */
    protected void writeTTFilesByUsers(TableWriter[] trainWriters, TableWriter[] testWriters,
                                        DataAccessObject dao) throws CommandException {
        logger.info("splitting data source {} to {} partitions by users",
                    getName(), partitionCount);
        Cursor<UserHistory<Rating>> historyCursor = dao.getUserHistories(Rating.class);
        Long2IntMap splits = splitUsers(dao);
        Holdout mode = this.getHoldout();
        try {
            for (UserHistory<Rating> history : historyCursor) {
                int foldNum = splits.get(history.getUserId());
                List<Rating> ratings = new ArrayList<Rating>(history);
                final int p = mode.partition(ratings, random);
                final int n = ratings.size();

                for (int f = 0; f < partitionCount; f++) {
                    if (f == foldNum) {
                        for (int j = 0; j < p; j++) {
                            writeRating(trainWriters[f], ratings.get(j));
                        }
                        for (int j = p; j < n; j++) {
                            writeRating(testWriters[f], ratings.get(j));
                        }
                    } else {
                        for (Rating rating : ratings) {
                            writeRating(trainWriters[f], rating);
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new CommandException("Error writing to the train test files", e);
        } finally {
            historyCursor.close();
        }
    }
    
    /**
     * Write the split files by Ratings from the DAO
     * 
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     * @param dao The DAO of the data source file
     * @throws CommandException
     */
    protected void writeTTFilesByRatings(TableWriter[] trainWriters, TableWriter[] testWriters, 
                                          DataAccessObject dao) throws CommandException {
        logger.info("splitting data source {} to {} partitions by ratings",
                    getName(), partitionCount);
        ArrayList<Rating> ratings = Cursors.makeList(dao.getEvents(Rating.class));
        Collections.shuffle(ratings);
        try {
            final int n = ratings.size();
            for (int i = 0; i < n; i++) {
                for (int f = 0; f < partitionCount; f++) {
                    int foldNum = i % partitionCount;
                    if (f == foldNum) {
                        writeRating(testWriters[f], ratings.get(i));
                    } else {
                        writeRating(trainWriters[f], ratings.get(i));
                    }
                }
            }
        } catch (IOException e) {
            throw new CommandException("Error writing to the train test files", e);
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
        String[] row = new String[4];
        row[0] = Long.toString(rating.getUserId());
        row[1] = Long.toString(rating.getItemId());
        Preference pref = rating.getPreference();
        row[2] = pref != null ? Double.toString(pref.getValue()) : "NaN";
        row[3] = Long.toString(rating.getTimestamp());
        writer.writeRow(row);
    }

    /**
     * Split users ids to n splits, where n is the partitionCount
     *
     * @param dao The DAO of the source file
     * @return a map of users to partition numbers.
     */
    protected Long2IntMap splitUsers(DataAccessObject dao) {
        Long2IntMap userMap = new Long2IntOpenHashMap();
        LongArrayList users = Cursors.makeList(dao.getUsers());
        LongLists.shuffle(users, random);
        LongListIterator iter = users.listIterator();
        while (iter.hasNext()) {
            final int idx = iter.nextIndex();
            final long user = iter.nextLong();
            userMap.put(user, idx % partitionCount);
        }

        logger.info("Partitioned {} users", userMap.size());
        return userMap;
    }
    
    /**
     * Get the train-test splits as data sets.
     * 
     * @return List<TTDataSet> The partition files stored as a list of TTDataSet
     */
    public List<TTDataSet> getTTFiles() {
        List<TTDataSet> dataSets = new ArrayList<TTDataSet>(partitionCount);
        File[] trainFiles = getFiles(getTrainPattern());
        File[] testFiles = getFiles(getTestPattern());
        for (int i = 0; i < partitionCount; i++) {
            CSVDataSourceCommand trainCommand = new CSVDataSourceCommand()
                    .setWrapper(wrapper)
                    .setDomain(source.getPreferenceDomain())
                    .setFile(trainFiles[i]);
            CSVDataSourceCommand testCommand = new CSVDataSourceCommand()
                    .setWrapper(wrapper)
                    .setDomain(source.getPreferenceDomain())
                    .setFile(testFiles[i]);
            GenericTTDataCommand tt = new GenericTTDataCommand(name + "." + i);

            dataSets.add(tt.setTest(testCommand.call())
                           .setTrain(trainCommand.call())
                           .setAttribute("DataSet", getName())
                           .setAttribute("Partition", i)
                           .call());
        }
        return dataSets;
    }

    @Override
    public String toString() {
        return String.format("{CXManager %s}", source);
    }
}
