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


    private static final String SPLIT_FILE = "split.dat";
    
    private static final Random random = new Random();

	private final DataSource source;
	private final int partitionCount;
    private final Holdout holdout;
    private List<TTDataSet> dataSets;
    private final Function<DAOFactory,DAOFactory> wrapper;

    private TableLayout outPutLayout;

	public CrossfoldSplit(String name, Set<EvalTask> dependency, DataSource src, int folds, Holdout hold,
                          String filename, Function<DAOFactory, DAOFactory> wrap) {
	    super(name, dependency);
        source = src;
	    partitionCount = folds;
        holdout = hold;
        wrapper = wrap;
        dataSets = new ArrayList<TTDataSet>(partitionCount);
        TableLayoutBuilder builder = new TableLayoutBuilder();
        builder.addColumn("User ID");
        builder.addColumn("Item ID");
        builder.addColumn("Rating");
        builder.addColumn("Timestamp");
        outPutLayout = builder.build();
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

    /**
     * Get the cache directory for this crossfolder.
     *
     * @return The directory in which to cache data. If the manager is prepared,
     *         this directory will exist.
     */
    public File cacheDir() {
	    String name = "crossfold-" + getName();
	    try {
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Java is broken", e);
        }
	    return new File(name);
	}

    @Override
    public void call(EvalTaskOptions options) throws EvalExecuteException {

        if(!options.isForce() && lastUpdated() >= source.lastUpdated()) {
            logger.debug("Crossfold {} up to date", this);
            return;
        }    
        
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject dao = factory.create();
        try{
            Long2IntMap splits = splitUsers(dao);
            writeSplits(splits);
        } catch (IOException e) {
            throw new EvalExecuteException("Error writing partitions to disk", e);
        } finally {
            dao.close();
        }
        Holdout mode = this.getHoldout();
        DataAccessObject daoSnap = factory.snapshot();
        for (int i = 1; i <= partitionCount; i++) {
            logger.debug("Preparing data source {}", getName());
            logger.debug("Writing {} train test files...", i);
            File train = new File(getName() + "train-" + i);
            File test = new File(getName() + "test-" + i);
            LongOpenHashSet testEvents = new LongOpenHashSet();
            try {
                LongList testUsers = this.getFoldUsers(i);
                LongIterator iter = testUsers.iterator();
                while (iter.hasNext()) {
                    UserHistory<Rating> history = dao.getUserHistory(iter.nextLong(), Rating.class);
                    List<Rating> ratings = new ArrayList<Rating>(history);
                    final int p = mode.partition(ratings);
                    final int n = ratings.size();
                    for (int j = p; j < n; j++) {
                        testEvents.add(ratings.get(j).getId());
                    }
                }
            } catch (Exception e) {
                throw new EvalExecuteException("Error partition the user data", e);
            }
            finally {
                dao.close();
            }

            EventSupplier trainP = new EventSupplier(factory, Predicates.not(testRatingPredicate(testEvents)));
            EventSupplier testP = new EventSupplier(factory, testRatingPredicate(testEvents));
            writeFile(train, trainP);
            writeFile(test, testP);
            CSVDataSourceBuilder trainBuilder = new CSVDataSourceBuilder();
            CSVDataSourceBuilder testBuilder = new CSVDataSourceBuilder();
            GenericTTDataBuilder TTbuilder = new GenericTTDataBuilder();

            dataSets.add(TTbuilder
                        .setTest(testBuilder.setFile(test).build())
                        .setTrain(trainBuilder.setFile(train).build())
                        .build());
        }

    }

	public long lastUpdated() {
	    File split = new File(cacheDir(), SPLIT_FILE);
	    return split.exists() ? split.lastModified() : -1L;
	}
    
    protected void writeFile(File file, EventSupplier supplier) throws RuntimeException{
        try{
            TableWriter outPut = CSVWriter.open(file, outPutLayout);
            for(Rating r :supplier.get()) {
                String[] row = new String[4];
                row[0] = Long.toString(r.getUserId());
                row[1] = Long.toString(r.getItemId());
                row[2] = r.getPreference()!=null ? Double.toString(r.getPreference().getValue()) : "NaN";
                row[3] = Long.toString(r.getTimestamp());
                try{
                    outPut.writeRow(row);
                } catch (IOException e) {
                    throw new RuntimeException("Error writing to the file", e);
                }
            }
            try{
                outPut.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing the file", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error opening output table", e);
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
	
	/**
	 * Write the user partitions out into a data file.
	 * 
	 * <p>This method uses a strategy based on the POSIX rename() pattern, but
	 * it does not have the safety guarantees of POSIX rename().  The file is
	 * deleted first if it exists, a temp file is then created and written to,
	 * and finally the temp file is renamed to the real file.  This should be
	 * safe so long as there aren't two processes trying to prepare with the
	 * same cache directory. 
	 *
	 * @param splits The user partition data.
	 * @throws IOException if there is an error writing the partitions to disk.
	 */
	protected void writeSplits( Long2IntMap splits) throws IOException {
	    File splitFile = new File(cacheDir(), SPLIT_FILE);
	    File tmpFile = new File(cacheDir(), SPLIT_FILE + ".tmp");
	    Files.createParentDirs(splitFile);
	    
	    logger.info("Writing user partitions to {}", splitFile);
	    
	    if (splitFile.exists()) {
	        logger.debug("Deleting {}", splitFile);
	        splitFile.delete();
	    }
	    
	    logger.debug("Writing to {}", tmpFile);
	    PrintWriter writer = new PrintWriter(tmpFile);
	    try {
	        writer.println(partitionCount);
	        for (Long2IntMap.Entry entry: splits.long2IntEntrySet()) {
	            writer.print(entry.getLongKey());
	            writer.print('\t');
	            writer.print(entry.getIntValue());
	            writer.println();
	        }
	    } finally {
	        writer.close();
	    }
	    
	    logger.debug("Renaming temp file");
	    if (!tmpFile.renameTo(splitFile)) {
	        throw new IOException("Failed to rename temp file");
	    }
	}
	
	public LongList getFoldUsers(int fold) {
	    File split = new File(cacheDir(), SPLIT_FILE);
	    
	    LongList users = new LongArrayList();
	    
	    Scanner input;
        try {
            input = new Scanner(split);
            try {
                int nfolds = input.nextInt();
                if (fold < 1 || fold > nfolds) {
                    throw new IllegalArgumentException("Invalid fold number " + fold);
                }
                while (input.hasNextLong()) {
                    long uid = input.nextLong();
                    int f = input.nextInt();
                    if (f == fold) {
                        users.add(uid);
                    }
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Crossfold manager not prepared", e);
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
