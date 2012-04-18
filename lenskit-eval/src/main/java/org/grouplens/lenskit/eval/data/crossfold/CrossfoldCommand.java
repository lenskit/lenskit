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
import org.grouplens.lenskit.util.tablewriter.CSVWriter;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 *
 */

public class CrossfoldCommand extends AbstractCommand<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldCommand.class);

    private static final Random random = new Random();

    private DataSource source;
    private int partitionCount = 5;
    private Holdout holdout;
    private String trainFilePattern;
    private String testFilePattern;
    private Order<Rating> order = new RandomOrder<Rating>();
    private PartitionAlgorithm<Rating> partition = new CountPartition<Rating>(10);
    private boolean isForced;

    @Nullable
    private Function<DAOFactory, DAOFactory> wrapper;
    

    public CrossfoldCommand() {
        super("Crossfold");
    }                   
    
    public CrossfoldCommand(String n) {
        super(n);
    }

    /**
     *  Set the number of partitions to generate.
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
     * @param n The number of items to hold out from each user's profile.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setHoldout(int n) {
        partition = new CountPartition<Rating>(n);
        return this;
    }

    /**
     * Set holdout to a fraction of each user's profile.
     * @param f The fraction of a user's ratings to hold out.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setHoldout(double f) {
        partition = new FractionPartition<Rating>(f);
        return this;
    }

    /**
     * Set the input data source.
     * @param source The data source to use.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setSource(DataSource source) {
        this.source = source;
        return this;
    }

    /**
     * Set a wrapper function for the constructed data sources.
     * @param wrapFun The wrapper function.
     * @return The CrossfoldCommand object  (for chaining)
     * @see org.grouplens.lenskit.eval.data.CSVDataSourceCommand#setWrapper(Function)
     */
    public CrossfoldCommand setWrapper(Function<DAOFactory,DAOFactory> wrapFun) {
        wrapper = wrapFun;
        return this;
    }

    /**
     * Set the force running option of the command. The crossfold will be forced to
     * ran with the isForced set to true regardless of whether the partition files
     * are up to date.
     * @param force The force to run option
     * @return The CrossfoldCommand object  (for chaining)
     */
    public CrossfoldCommand setForce(boolean force) {
        isForced = force;
        return this;
    }

    public CrossfoldCommand build() {
        if (trainFilePattern == null) {
            trainFilePattern = name + ".train.%d.csv";
        }
        if (testFilePattern == null) {
            testFilePattern = name + ".test.%d.csv";
        }
        holdout = new Holdout(order, partition);
        return this;
    }

    /**
     * Get the visible name of this crossfold split.
     * @return The name of the crossfold split.
     */
    public String getName() {
        if (name.equals("Crossfold")) {
            return source.getName();
        } else {
            return name;
        }
    }

    public String getTrainPattern() {
        return trainFilePattern;
    }

    public String getTestPattern() {
        return testFilePattern;
    }

    /**
     * Get the data source backing this crossfold manager.
     * @return The underlying data source.
     */
    public DataSource getSource() {
        return source;
    }

    /**
     * Get the number of folds.
     * @return The number of folds in this crossfold.
     */
    public int getPartitionCount() {
        return partitionCount;
    }

    public Holdout getHoldout() {
        return holdout;
    }

    public boolean getForce() {
        return isForced;
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     *
     * @return List<TTDataSet> The partition files stored as a list of TTDataSet
     * @throws org.grouplens.lenskit.eval.CommandException
     */
    @Override
    public List<TTDataSet> call() throws CommandException {
        this.build();
        if(!isForced) {
            long mtime = lastModified();
            long srcMtime = source.lastModified();
            logger.debug("crossfold {} last modified at {}", getName(), mtime);
            logger.debug("source {} last modified at {}", getName(), srcMtime);
            if (mtime >= srcMtime) {
                logger.info("crossfold {} up to date", getName());
                return getTTFiles();
            }
        }
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject dao = factory.create();
        Long2IntMap splits;
        try {
            splits = splitUsers(dao);
        } finally {
            dao.close();
        }
        Holdout mode = this.getHoldout();
        DataAccessObject daoSnap = factory.snapshot();
        try {
            logger.info("splitting data source {} to {} partitions",
                    getName(), partitionCount);
            createTTFiles(daoSnap, mode, splits);
        } finally {
            daoSnap.close();
        }
        return getTTFiles();
    }


    /**
     * Get the list of files satisfying the specified name pattern
     * @param pattern  The file name pattern
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
     * Get the last modification time of all the crossfold split files. The modified time is the
     * oldest modified time of all the files.
     *
     * @return The modification time
     */
    public long lastModified() {
        File[] trainFiles = getFiles(trainFilePattern);
        File[] testFiles = getFiles(testFilePattern);
        Long mtime = null;
        for (File f: trainFiles) {
            if (!f.exists()) {
                mtime = -1L;
            } else if (mtime == null) {
                mtime = f.lastModified();
            } else {
                mtime = Math.min(mtime, f.lastModified());
            }
        }
        for (File f: testFiles) {
            if (!f.exists()) {
                mtime = -1L;
            } else if (mtime == null) {
                mtime = f.lastModified();
            } else {
                mtime = Math.min(mtime, f.lastModified());
            }
        }
        if (mtime == null) {
            mtime = -1L;
        }
        return mtime;
    }

    /**
     * Create the split files from the DAO using specified holdout method.
     *
     * @param dao       The DAO of the data source file
     * @param mode      Holdout mode
     * @param splits    The map of user id to the split number of all the users
     * @throws org.grouplens.lenskit.eval.CommandException Any error
     */
    protected void createTTFiles(DataAccessObject dao, Holdout mode, Long2IntMap splits) throws CommandException {
        File[] trainFiles = getFiles(trainFilePattern);
        File[] testFiles = getFiles(testFilePattern);
        TableWriter[] trainWriters = new TableWriter[partitionCount];
        TableWriter[] testWriters = new TableWriter[partitionCount];
        try {
            for (int i = 0; i < partitionCount; i++) {
                File train = trainFiles[i];
                File test = testFiles[i];
                try{
                    trainWriters[i] = CSVWriter.open(train, null);
                    testWriters[i] = CSVWriter.open(test, null);
                } catch (IOException e) {
                    throw new CommandException("Error creating train test file writer", e);
                }
            }
            Cursor<UserHistory<Rating>> historyCursor = dao.getUserHistories(Rating.class);
            try {
                for(UserHistory<Rating> history: historyCursor) {
                    int foldNum = splits.get(history.getUserId());
                    List<Rating> ratings = new ArrayList<Rating>(history);
                    final int p = mode.partition(ratings, random);
                    final int n = ratings.size();

                    for (int f = 0; f < partitionCount; f++) {
                        if(f == foldNum) {
                            for (int j = p; j < n; j++) {
                                writeRating(testWriters[f], ratings.get(j));
                            }
                            for (int j = 0; j < p; j++) {
                                writeRating(trainWriters[f], ratings.get(j));
                            }
                        }
                        else {
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
        } finally {
            LKFileUtils.close(logger, trainWriters);
            LKFileUtils.close(logger, testWriters);
        }
    }

    /**
     * Writing a rating event to the file using table writer
     *
     * @param writer The table writer to output the rating
     * @param rating The rating event to output
     * @throws IOException The writer IO error
     */
    protected void writeRating(TableWriter writer, Rating rating) throws IOException{
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

    public List<TTDataSet> getTTFiles() {
        List<TTDataSet> dataSets = new ArrayList<TTDataSet>(partitionCount);
        File[] trainFiles = getFiles(trainFilePattern);
        File[] testFiles = getFiles(testFilePattern);
        for(int i = 0; i < partitionCount; i++) {
            CSVDataSourceCommand trainCommand = new CSVDataSourceCommand();
            CSVDataSourceCommand testCommand = new CSVDataSourceCommand();
            trainCommand.setWrapper(wrapper);
            testCommand.setWrapper(wrapper);
            GenericTTDataCommand TTcommand = new GenericTTDataCommand();

            dataSets.add(TTcommand.setTest(testCommand.setFile(testFiles[i]).call())
                    .setTrain(trainCommand.setFile(trainFiles[i]).call())
                    .setAttribute("DataSet", name)
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
