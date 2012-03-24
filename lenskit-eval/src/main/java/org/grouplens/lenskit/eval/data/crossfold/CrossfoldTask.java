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
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.AbstractEvalTask;
import org.grouplens.lenskit.eval.EvalTask;
import org.grouplens.lenskit.eval.EvalTaskFailedException;
import org.grouplens.lenskit.eval.GlobalEvalOptions;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.LKFileUtils;
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

    @Override
    public void execute(GlobalEvalOptions options) throws EvalTaskFailedException {
        if(!options.isForce() && lastModified() >= source.lastModified()) {
            logger.debug("Crossfold {} up to date", this);
            return;
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
            logger.debug("Preparing data source {}", getName());
            logger.debug("Writing train test files...");
            createTTFiles(daoSnap, mode, splits);
        } finally {
            daoSnap.close();
        }
    }

    protected File[] getFiles(String pattern) {
        File[] files = new File[partitionCount];
        for (int i = 0; i < partitionCount; i++) {
            files[i] = new File(String.format(pattern, i));
        }
        return files;
    }

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
        try {
            for(UserHistory<Event> userHist : dao.getUserHistories()) {
                int foldNum = splits.get(userHist.getUserId());
                List<Rating> ratings = new ArrayList<Rating>(userHist.filter(Rating.class));
                final int p = mode.partition(ratings);
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
        }
        } finally {
            LKFileUtils.close(logger, trainWriters);
            LKFileUtils.close(logger, testWriters);
        }
    }

    protected void writeRating(TableWriter writer, Rating rating) throws IOException{
        String[] row = new String[4];
        row[0] = Long.toString(rating.getUserId());
        row[1] = Long.toString(rating.getItemId());
        row[2] = rating.getPreference()!=null ? Double.toString(rating.getPreference().getValue()) : "NaN";
        row[3] = Long.toString(rating.getTimestamp());
        writer.writeRow(row);
    }
	
	protected Long2IntMap splitUsers(DataAccessObject dao) {
        Long2IntMap userMap = new Long2IntOpenHashMap();
        LongArrayList users = Cursors.makeList(dao.getUsers());
        // Randomly allocate users in a Fisher-Yates shuffle
        // FIXME Make this use LongLists.shuffle
        int fold = 0;
        for (int i = users.size() - 1; i > 0; i--) {
            int j = random.nextInt(i+1);
            // put users[j] in a fold
            long u = j;
            userMap.put(u, fold);
            fold = (fold + 1) % partitionCount;
            // replace users[j] with users[i] to remove used user
            if (i != j) {
                users.set(j, users.get(i));
            }
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
