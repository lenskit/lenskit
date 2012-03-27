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
import com.google.common.base.Supplier;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.AbstractEvalTask;
import org.grouplens.lenskit.eval.EvalOptions;
import org.grouplens.lenskit.eval.EvalTask;
import org.grouplens.lenskit.eval.EvalTaskFailedException;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
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
import java.util.Set;

/**
 * A crossfold split, taking a data set and splitting it into multiple train-test sets.
 * The users in the input data set are first partitioned into \(N\) disjoint sets; for
 * each set of users, a train-test set is built. The user profile is split according to
 * the holdout into a train set and a test set; for each partition, a training set is
 * built from the training sets of its users combined with the ratings from all other
 * users, and the test set is the test sets of its users.
 */
public class CrossfoldTask extends AbstractEvalTask implements Supplier<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldTask.class);
    
    private static final Random random = new Random();

	private final DataSource source;
	private final int partitionCount;
    private final Holdout holdout;
    private final String trainFilePattern;
    private final String testFilePattern;
    @Nullable
    private Function<DAOFactory, DAOFactory> wrapper;

    public CrossfoldTask(String name, Set<EvalTask> dependencies, DataSource src, int folds, Holdout hold,
                         String trainPattern, String testPattern,
                         @Nullable Function<DAOFactory,DAOFactory> wrapFun) {
	    super(name, dependencies);
        source = src;
	    partitionCount = folds;
        holdout = hold;
        trainFilePattern = trainPattern;
        testFilePattern = testPattern;
        wrapper = wrapFun;
    }

    /**
     * Get the visible name of this crossfold split.
     * @return The name of the crossfold split.
     */
    public String getName() {
        if (name == null) {
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

    /**
     * Write the crossfold files to the disk by reading in the original data source file
     *
     * @throws EvalTaskFailedException
     */
    @Override
    public void execute(EvalOptions options) throws EvalTaskFailedException {
        if(!options.isForce()) {
            long mtime = lastModified();
            long srcMtime = source.lastModified();
            logger.debug("crossfold {} last modified at {}", getName(), mtime);
            logger.debug("source {} last modified at {}", getName(), srcMtime);
            if (mtime >= srcMtime) {
                logger.info("crossfold {} up to date", getName());
                return;
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
    }

    /**
     * Get the list of files satisfying the specified name pattern
     * @param pattern  The file name pattern
     * @return
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
     * @throws EvalTaskFailedException
     */
    protected void createTTFiles(DataAccessObject dao, Holdout mode, Long2IntMap splits) throws EvalTaskFailedException {
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
                    throw new EvalTaskFailedException("Error creating train test file writer", e);
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
                throw new EvalTaskFailedException("Error writing to the train test files", e);
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
     * @throws IOException
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

    @Override
    public List<TTDataSet> get() {
        List<TTDataSet> dataSets = new ArrayList<TTDataSet>(partitionCount);
        File[] trainFiles = getFiles(trainFilePattern);
        File[] testFiles = getFiles(testFilePattern);
        for(int i = 0; i < partitionCount; i++) {
            CSVDataSourceBuilder trainBuilder = new CSVDataSourceBuilder();
            CSVDataSourceBuilder testBuilder = new CSVDataSourceBuilder();
            trainBuilder.setWrapper(wrapper);
            testBuilder.setWrapper(wrapper);
            GenericTTDataBuilder TTbuilder = new GenericTTDataBuilder();

            dataSets.add(TTbuilder.setTest(testBuilder.setFile(testFiles[i]).build())
                                  .setTrain(trainBuilder.setFile(trainFiles[i]).build())
                                  .build());
        }
        return dataSets;
    }
	
	@Override
	public String toString() {
	    return String.format("{CXManager %s}", source);
	}
}
