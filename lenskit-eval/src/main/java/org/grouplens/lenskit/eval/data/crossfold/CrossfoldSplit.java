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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.io.Files;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.*;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.tablewriter.CSVWriter;
import org.grouplens.lenskit.util.tablewriter.TableLayout;
import org.grouplens.lenskit.util.tablewriter.TableLayoutBuilder;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * A crossfold split, taking a data set and splitting it into multiple train-test sets.
 * The users in the input data set are first partitioned into \(N\) disjoint sets; for
 * each set of users, a train-test set is built. The user profile is split according to
 * the holdout into a train set and a test set; for each partition, a training set is
 * built from the training sets of its users combined with the ratings from all other
 * users, and the test set is the test sets of its users.
 */
public class CrossfoldSplit extends AbstractEvalTask implements Supplier<List<TTDataSet>> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldSplit.class);
    
    private static final Random random = new Random();

	private final DataSource source;
	private final int partitionCount;
    private final Holdout holdout;
    private List<TTDataSet> dataSets;
    private final Function<DAOFactory,DAOFactory> wrapper;
    private String fileName = "fold-%d";


	public CrossfoldSplit(String name, Set<EvalTask> dependencies, DataSource src, int folds, Holdout hold,
                          String fname, Function<DAOFactory, DAOFactory> wrap) {
	    super(name, dependencies);
        source = src;
	    partitionCount = folds;
        holdout = hold;
        fileName = fname;
        wrapper = wrap;
        dataSets = new ArrayList<TTDataSet>(partitionCount);

        
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
    
    public String getFileName() {
        return fileName;
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
     * Get the function used to wrap DAOs.
     * @return The DAO wrapper function, or {@code null} if no such function is set.
     * @see CrossfoldBuilder#setWrapper(Function)
     */
    public Function<DAOFactory, DAOFactory> getDAOWrapper() {
        return wrapper;
    }

    @Override
    public void call(EvalTaskOptions options) throws EvalExecuteException {

        if(!options.isForce() && lastModified() >= source.lastUpdated()) {
            logger.debug("Crossfold {} up to date", this);
            return;
        }    
        
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject dao = factory.create();
        Long2IntMap splits = splitUsers(dao);
        dao.close();
        Holdout mode = this.getHoldout();
        DataAccessObject daoSnap = factory.snapshot();
        logger.debug("Preparing data source {}", getName());
        logger.debug("Writing train test files...");
        File[] trainFiles = new File[partitionCount];
        File[] testFiles = new File[partitionCount];
        TableWriter[] trainWriters = new TableWriter[partitionCount];
        TableWriter[] testWriters = new TableWriter[partitionCount];
        for (int i = 0; i < partitionCount; i++) {
            File train = new File(String.format(getFileName(), i) + "-train");
            File test = new File(String.format(getFileName(), i) + "-test");
            trainFiles[i] = train;
            testFiles[i] = test;
            try{
                trainWriters[i] = CSVWriter.open(train, null);
                testWriters[i] = CSVWriter.open(test, null);
            } catch (IOException e) {
                throw new EvalExecuteException("Error creating train test file writer", e);
            }
        }
        try {
            for(UserHistory<Event> userHist : daoSnap.getUserHistories()) {
                int foldNum = splits.get(userHist.getUserId());
                List<Rating> ratings = new ArrayList<Rating>(userHist.filter(Rating.class));
                final int p = mode.partition(ratings);
                final int n = ratings.size();

                for (int f = 1; f <= partitionCount; f++) {
                    if(f == foldNum) {
                        for (int j = p; j < n; j++) {
                            writeFile(testWriters[foldNum - 1], ratings.get(j));
                        }
                        for (int j = 0; j < p; j++) {
                            writeFile(trainWriters[foldNum - 1], ratings.get(j));
                        }
                    }
                    else {
                        for (Rating rating : ratings) {
                            writeFile(trainWriters[f - 1], rating);
                        }
                    }
                }

            }
            for(int i = 0; i < partitionCount; i++) {
                trainWriters[i].close();
                testWriters[i].close();
            }
        } catch (IOException e) {
            throw new EvalExecuteException("Error writing to the train test files", e);
        } finally {
            daoSnap.close();
        }
        for(int i = 0; i < partitionCount; i++) {
            CSVDataSourceBuilder trainBuilder = new CSVDataSourceBuilder();
            CSVDataSourceBuilder testBuilder = new CSVDataSourceBuilder();
            GenericTTDataBuilder TTbuilder = new GenericTTDataBuilder();

            dataSets.add(TTbuilder
                    .setTest(testBuilder.setFile(testFiles[i]).build())
                    .setTrain(trainBuilder.setFile(trainFiles[i]).build())
                    .build());
        }
    }

    

	public long lastModified() {
	    File split = new File(String.format(getFileName(), 1) + "-train");
	    return split.exists() ? split.lastModified() : -1L;
	}
    
    protected void writeFile(TableWriter writer, Rating rating) throws IOException{
        String[] row = new String[4];
        row[0] = Long.toString(rating.getUserId());
        row[1] = Long.toString(rating.getItemId());
        row[2] = rating.getPreference()!=null ? Double.toString(rating.getPreference().getValue()) : "NaN";
        row[3] = Long.toString(rating.getTimestamp());
        try{
            writer.writeRow(row);
        } finally {
            writer.close();
        }
    }
	
	protected Long2IntMap splitUsers(DataAccessObject dao) {
        Long2IntMap userMap = new Long2IntOpenHashMap();
        LongArrayList users = Cursors.makeList(dao.getUsers());
        // Randomly allocate users in a Fisher-Yates shuffle
        int fold = 0;
        for (int i = users.size() - 1; i > 0; i--) {
            int j = random.nextInt(i+1);
            // put users[j] in a fold
            long u = j;
            userMap.put(u, fold+1);
            fold = (fold + 1) % partitionCount;
            // replace users[j] with users[i] to remove used user
            if (i != j) {
                users.set(j, users.get(i));
            }
        }
        
        logger.info("Partitioned {} users", userMap.size());
        return userMap;
    }


    public LongSet getFoldUsers(int fold,  Long2IntMap splits) {
        LongSet users = new LongArraySet();
       if (fold < 1 || fold > partitionCount) {
            throw new IllegalArgumentException("Invalid fold number " + fold);
        }
        for (Long2IntMap.Entry entry : splits.long2IntEntrySet()){
            long uid = entry.getLongKey();
            int f = entry.getIntValue();
            if (f == fold) {
                users.add(uid);
            }
        }
        return users;
    }

    @Override
    public List<TTDataSet> get() {
        return dataSets;
    }

    protected Predicate<Rating> testRatingPredicate(final Set set) {
        return new Predicate<Rating>() {
            @Override public boolean apply(Rating r) {
                return set.contains(r.getId());
            }
        };
    }
	
	@Override
	public String toString() {
	    return String.format("{CXManager %s}", source);
	}

    static class EventSupplier implements Supplier<List<Rating>> {
        final Predicate<? super Rating> predicate;
        final DAOFactory baseFactory;

        public EventSupplier(DAOFactory base, Predicate<? super Rating> pred) {
            baseFactory = base;
            predicate = pred;
        }

        @Override
        public List<Rating> get() {
            DataAccessObject bdao = baseFactory.create();
            List<Rating> ratings;
            try {
                Cursor<Rating> cursor =
                        Cursors.filter(bdao.getEvents(Rating.class), predicate);
                ratings = Cursors.makeList(cursor);
            } finally {
                bdao.close();
            }
            return ratings;
        }
    }
}
