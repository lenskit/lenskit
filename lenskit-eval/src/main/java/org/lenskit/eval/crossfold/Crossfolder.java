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
package org.lenskit.eval.crossfold;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.source.CSVDataSourceBuilder;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.data.source.PackedDataSourceBuilder;
import org.grouplens.lenskit.eval.data.RatingWriter;
import org.grouplens.lenskit.eval.data.RatingWriters;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.specs.SpecHandlerInterface;
import org.grouplens.lenskit.specs.SpecificationContext;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.json.simple.JSONValue;
import org.lenskit.eval.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SpecHandlerInterface(CrossfoldSpecHandler.class)
public class Crossfolder implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Crossfolder.class);

    private Random rng;
    private String name;
    private DataSource source;
    private int partitionCount = 5;
    private Path outputDir;
    private OutputFormat outputFormat = OutputFormat.CSV;
    private Holdout holdout = new Holdout(new RandomOrder<Rating>(), new HoldoutNPartition<Rating>(10));
    private boolean skipIfUpToDate = false;
    private CrossfoldMethod method = CrossfoldMethod.PARTITION_USERS;
    private int sampleSize = 1000;
    private boolean isolate = false;
    private boolean writeTimestamps = true;

    public Crossfolder() {
        this(null);
    }

    public Crossfolder(String n) {
        name = n;
        rng = new Random();
    }

    /**
     * Set the number of partitions to generate.
     *
     * @param partition The number of paritions
     * @return The CrossfoldCommand object  (for chaining)
     */
    public Crossfolder setPartitions(int partition) {
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
    public Crossfolder setSampleSize(int n) {
        sampleSize = n;
        return this;
    }

    /**
     * Set the output format for the crossfolder.
     * @param format The output format.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setOutputFormat(OutputFormat format) {
        outputFormat = format;
        return this;
    }

    /**
     * Get the output format for the crossfolder.
     * @return The format the crossfolder will use for writing its output.
     */
    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Set the output directory for this crossfold operation.
     * @param dir The output directory.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setOutputDir(Path dir) {
        outputDir = dir;
        return this;
    }

    /**
     * Set the output directory for this crossfold operation.
     * @param dir The output directory.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setOutputDir(File dir) {
        return setOutputDir(dir.toPath());
    }

    /**
     * Set the output directory for this crossfold operation.
     * @param dir The output directory.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setOutputDir(String dir) {
        return setOutputDir(Paths.get(dir));
    }

    /**
     * Get the output directory.
     * @return The directory into which crossfolding output will be placed.
     */
    public Path getOutputDir() {
        if (outputDir != null) {
            return outputDir;
        } else {
            return Paths.get(getName() + ".split");
        }
    }

    /**
     * Set the holdout method for preparing train-test splits from a user's ratings.  Will only be used if one of the
     * user-based {@linkplain #setMethod(CrossfoldMethod) methods} is selected.
     *
     * @param ho The per-user holdout method.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setHoldout(Holdout ho) {
        holdout = ho;
        return this;
    }

    /**
     * Set the holdout method for preparing train-test splits from a user's ratings.  Will only be used if one of the
     * user-based {@linkplain #setMethod(CrossfoldMethod) methods} is selected.
     *
     * @param order The holdout order.
     * @param part The rating partition method.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setHoldout(Order<Rating> order, PartitionAlgorithm<Rating> part) {
        return setHoldout(new Holdout(order, part));
    }

    /**
     * Get the per-user holdout method.
     * @return The per-user holdout method.
     */
    public Holdout getHoldout() {
        return holdout;
    }

    /**
     * Set the input data source.
     *
     * @param source The data source to use.
     * @return The CrossfoldCommand object  (for chaining)
     */
    public Crossfolder setSource(DataSource source) {
        this.source = source;
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
    public Crossfolder setMethod(CrossfoldMethod m) {
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
    public Crossfolder setCache(boolean on) {
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
    public Crossfolder setIsolate(boolean on) {
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
    public Crossfolder setWriteTimestamps(boolean pack) {
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
    public String getName() {
        if (name == null) {
            return source.getName();
        } else {
            return name;
        }
    }

    /**
     * Set a name for this crossfolder.  It will be used to generate the names of individual data sets, for example.
     * @param n The crossfolder name.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setName(String n) {
        name = n;
        return this;
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

    /**
     * Set whether the crossfolder should skip if all files are up to date.  The default is to always re-crossfold, even
     * if the files are up to date.
     *
     * @param skip `true` to skip crossfolding if files are up to date.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setSkipIfUpToDate(boolean skip) {
        skipIfUpToDate = skip;
        return this;
    }

    public boolean getSkipIfUpToDate() {
        return skipIfUpToDate;
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     */
    public void run() {
        if (skipIfUpToDate) {
            UpToDateChecker check = new UpToDateChecker();
            check.addInput(source.lastModified());
            for (Path p: Iterables.concat(getTrainingFiles(), getTestFiles(), getSpecFiles())) {
                check.addOutput(p.toFile());
            }
            if (check.isUpToDate()) {
                logger.info("crossfold {} up to date", getName());
                return;
            }
        }
        try {
            createTTFiles();
        } catch (IOException ex) {
            // TODO Use application-specific exception
            throw new RuntimeException("Error writing data sets", ex);
        }
    }

    private List<Path> getTrainingFiles() {
        return getFileList("train.%d." + outputFormat.getSuffix());
    }

    private List<Path> getTestFiles() {
        return getFileList("test.%d." + outputFormat.getSuffix());
    }

    private List<Path> getSpecFiles() {
        return getFileList("spec.%d.json");
    }

    private List<Path> getFileList(String pattern) {
        List<Path> files = new ArrayList<>(partitionCount);
        for (int i = 1; i <= partitionCount; i++) {
            files.add(getOutputDir().resolve(String.format(pattern, i)));
        }
        return files;
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
     * Write train-test split files.
     *
     * @throws IOException if there is an error writing the files.
     */
    private void createTTFiles() throws IOException {
        // this method is going to proceed through several others
        // first, it will set up data structures for building writers, etc.
        List<Path> trainFiles = getTrainingFiles();
        List<Path> testFiles = getTestFiles();
        RatingWriter[] trainWriters = new RatingWriter[partitionCount];
        RatingWriter[] testWriters = new RatingWriter[partitionCount];

        // now, the openWriteAndCloseFiles method will open each output file in turn, then call the writer method.
        openWriteAndCloseFiles(trainFiles, testFiles, trainWriters, testWriters, 0);

        List<Path> specFiles = getSpecFiles();
        List<TTDataSet> dataSets = getDataSets();
        Path fullSpecFile = getOutputDir().resolve("all-partitions.json");
        SpecificationContext fullCtx = SpecificationContext.create(fullSpecFile.toUri());
        List<Object> specs = new ArrayList<>(partitionCount);
        assert dataSets.size() == partitionCount;
        for (int i = 0; i < partitionCount; i++) {
            Path file = specFiles.get(i);
            TTDataSet ds = dataSets.get(i);
            SpecificationContext ctx = SpecificationContext.create(file.toUri());
            specs.add(ds.toSpecification(fullCtx));

            try (BufferedWriter w = Files.newBufferedWriter(file, Charsets.UTF_8,
                                                            StandardOpenOption.CREATE,
                                                            StandardOpenOption.TRUNCATE_EXISTING)) {
                JSONValue.writeJSONString(ds.toSpecification(ctx), w);
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(fullSpecFile, Charsets.UTF_8,
                                                        StandardOpenOption.CREATE,
                                                        StandardOpenOption.TRUNCATE_EXISTING)) {
            JSONValue.writeJSONString(specs, w);
        }
    }

    /**
     * Helper method that opens the output files and then writes to them.  This method is recursive so that we can use
     * Java's built-in resource management to make sure that our writers all get closed properly.
     *
     * @param trainFiles The train files.
     * @param testFiles The test files.
     * @param trainWriters The train writers (initially empty).
     * @param testWriters The test writers (initially empty).
     * @param i The current iteration (the initial call should pass 0 here).
     */
    private void openWriteAndCloseFiles(List<Path> trainFiles, List<Path> testFiles, RatingWriter[] trainWriters, RatingWriter[] testWriters, int i) throws IOException {
        assert trainFiles.size() == testFiles.size();
        assert trainWriters.length == trainFiles.size();
        assert testWriters.length == testFiles.size();

        if (i == testWriters.length) {
            // we have opened all files, now write them
            writeOutputFiles(trainWriters, testWriters);
        } else {
            // use a try-with-resources block to open the writers for partition 'i'
            // then go recursive to open the next set
            try (RatingWriter train = makeWriter(trainFiles.get(i));
                 RatingWriter test = makeWriter(testFiles.get(i))) {
                trainWriters[i] = train;
                testWriters[i] = test;
                openWriteAndCloseFiles(trainFiles, testFiles, trainWriters, testWriters, i + 1);
            }
        }
    }

    /**
     * Actually write ratings to the output files opened for each partition.
     * @param trainWriters The train data writers.
     * @param testWriters The test data writers.
     * @throws IOException if there is an error writing data
     */
    private void writeOutputFiles(RatingWriter[] trainWriters, RatingWriter[] testWriters) throws IOException {
        switch (method) {
        case PARTITION_USERS:
        case SAMPLE_USERS:
            writeTTFilesByUsers(trainWriters, testWriters);
            break;
        case PARTITION_RATINGS:
            writeTTFilesByRatings(trainWriters, testWriters);
            break;
        }
    }

    /**
     * Write the split files by Users from the DAO using specified holdout method
     * 
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     */
    protected void writeTTFilesByUsers(RatingWriter[] trainWriters, RatingWriter[] testWriters) throws IOException {
        logger.info("splitting data source {} to {} partitions by users",
                    getName(), partitionCount);
        Long2IntMap splits = splitUsers(source.getUserDAO());
        Cursor<UserHistory<Rating>> historyCursor = source.getUserEventDAO().streamEventsByUser(Rating.class);
        Holdout mode = this.getHoldout();
        try {
            for (UserHistory<Rating> history : historyCursor) {
                int foldNum = splits.get(history.getUserId());
                List<Rating> ratings = new ArrayList<Rating>(history);
                final int n = ratings.size();

                for (int f = 0; f < partitionCount; f++) {
                    if (f == foldNum) {
                        final int p = mode.partition(ratings, rng);
                        for (int j = 0; j < p; j++) {
                            trainWriters[f].writeRating(ratings.get(j));
                        }
                        for (int j = p; j < n; j++) {
                            testWriters[f].writeRating(ratings.get(j));
                        }
                    } else {
                        for (Rating rating : ratings) {
                            trainWriters[f].writeRating(rating);
                        }
                    }
                }

            }
        } finally {
            historyCursor.close();
        }
    }
    
    /**
     * Write the split files by Ratings from the DAO
     * 
     * @param trainWriters The tableWriter that write train files
     * @param testWriters  The tableWriter that writ test files
     */
    protected void writeTTFilesByRatings(RatingWriter[] trainWriters, RatingWriter[] testWriters) throws IOException {
        logger.info("splitting data source {} to {} partitions by ratings",
                    getName(), partitionCount);
        ArrayList<Rating> ratings = Cursors.makeList(source.getEventDAO().streamEvents(Rating.class));
        Collections.shuffle(ratings);

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
            LongArrays.shuffle(userArray, rng);
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
        logger.info("Splitting {} users into {} partitions", users.size(), partitionCount);
        long[] userArray = users.toLongArray();
        LongArrays.shuffle(userArray, rng);
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
    public List<TTDataSet> getDataSets() {
        List<TTDataSet> dataSets = new ArrayList<TTDataSet>(partitionCount);
        List<Path> trainFiles = getTrainingFiles();
        List<Path> testFiles = getTestFiles();
        for (int i = 0; i < partitionCount; i++) {
            GenericTTDataBuilder ttBuilder = new GenericTTDataBuilder(getName() + "." + i);
            if (isolate) {
                ttBuilder.setIsolationGroup(UUID.randomUUID());
            }

            dataSets.add(ttBuilder.setTest(makeDataSource(testFiles.get(i)))
                                  .setTrain(makeDataSource(trainFiles.get(i)))
                                  .setAttribute("DataSet", getName())
                                  .setAttribute("Partition", i)
                                  .build());
        }
        return dataSets;
    }

    protected RatingWriter makeWriter(Path file) throws IOException {
        if (outputFormat.equals(OutputFormat.PACK)) {
            EnumSet<BinaryFormatFlag> flags = BinaryFormatFlag.makeSet();
            if (writeTimestamps) {
                flags.add(BinaryFormatFlag.TIMESTAMPS);
            }
            return RatingWriters.packed(file.toFile(), flags);
        } else {
            // it is a CSV file
            return RatingWriters.csv(file.toFile(), writeTimestamps);
        }
    }

    protected DataSource makeDataSource(Path file) {
        switch (outputFormat) {
        case PACK:
            return new PackedDataSourceBuilder()
                    .setDomain(source.getPreferenceDomain())
                    .setFile(file.toFile())
                    .build();
        default:
            // TODO Don't just encode compression in file name
            return new CSVDataSourceBuilder()
                    .setDomain(source.getPreferenceDomain())
                    .setFile(file.toFile())
                    .build();
        }
    }

    @Override
    public String toString() {
        return String.format("{CXManager %s}", source);
    }
}
