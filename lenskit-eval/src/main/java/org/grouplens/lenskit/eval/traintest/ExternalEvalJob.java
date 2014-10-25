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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.data.CSVDataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.DelimitedTextCursor;
import org.grouplens.lenskit.util.io.LoggingStreamSlurper;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Job implementation for exeternal algorithms.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ExternalEvalJob extends TrainTestJob {
    private static final Logger logger = LoggerFactory.getLogger(ExternalEvalJob.class);
    private final ExternalAlgorithm algorithm;
    private final UUID key;
    private Long2ObjectMap<SparseVector> userPredictions;
    // References to hold on to the user event DAOs for test users
    private UserEventDAO userTrainingEvents, userTestEvents;

    public ExternalEvalJob(TrainTestEvalTask task,
                           @Nonnull ExternalAlgorithm algo,
                           @Nonnull TTDataSet ds) {
        super(task, algo, ds);
        algorithm = algo;
        key = UUID.randomUUID();
    }

    @Override
    protected void buildRecommender() throws RecommenderBuildException {
        Preconditions.checkState(userPredictions == null, "recommender already built");
        File dir = getStagingDir();
        logger.info("using output/staging directory {}", dir);
        if (!dir.exists()) {
            logger.info("creating directory {}", dir);
            dir.mkdirs();
        }

        final File train;
        try {
            train = trainingFile(dataSet);
        } catch (IOException e) {
            throw new RecommenderBuildException("error preparing training file", e);
        }
        final File test;
        try {
            test = testFile(dataSet);
        } catch (IOException e) {
            throw new RecommenderBuildException("error preparing test file", e);
        }
        final File output = getFile("predictions.csv");
        List<String> args = Lists.transform(algorithm.getCommand(), new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                if (input == null) {
                    throw new NullPointerException("command element");
                }
                String s = input.replace("{OUTPUT}", output.getAbsolutePath());
                s = s.replace("{TRAIN_DATA}", train.getAbsolutePath());
                s = s.replace("{TEST_DATA}", test.getAbsolutePath());
                return s;
            }
        });

        logger.info("running {}", StringUtils.join(args, " "));

        Process proc;
        try {
            proc = new ProcessBuilder().command(args)
                                       .directory(algorithm.getWorkDir())
                                       .start();
        } catch (IOException e) {
            throw new RecommenderBuildException("error creating process", e);
        }
        Thread listen = new LoggingStreamSlurper("external-algo", proc.getErrorStream(),
                                                 logger, "external: ");
        listen.run();

        int result = -1;
        boolean done = false;
        while (!done) {
            try {
                result = proc.waitFor();
                done = true;
            } catch (InterruptedException e) {
                logger.info("thread interrupted, killing subprocess");
                proc.destroy();
                throw new RecommenderBuildException("recommender build interrupted", e);
            }
        }

        if (result != 0) {
            logger.error("external command exited with status {}", result);
            throw new RecommenderBuildException("recommender exited with code " + result);
        }

        Long2ObjectMap<SparseVector> vectors;
        try {
            vectors = readPredictions(output);
        } catch (FileNotFoundException e) {
            logger.error("cannot find expected output file {}", output);
            throw new RecommenderBuildException("recommender produced no output", e);
        }

        userPredictions = vectors;

        userTrainingEvents = dataSet.getTrainingData().getUserEventDAO();
        userTestEvents = dataSet.getTestData().getUserEventDAO();
    }

    @Override
    protected TestUser getUserResults(long uid) {
        Preconditions.checkState(userPredictions != null, "recommender not built");
        return new TestUserImpl(uid);
    }

    @Override
    protected void cleanup() {
        userPredictions = null;
        userTrainingEvents = null;
        userTestEvents = null;
    }

    private File getStagingDir() {
        String dirName = String.format("%s-%s", algorithm.getName(), key);
        return new File(algorithm.getWorkDir(), dirName);
    }

    /**
     * Create a fully-qualified algorithm file name.
     * @param fn The file name.
     * @return A file in the working directory.
     */
    private File getFile(String fn) {
        return new File(getStagingDir(), fn);
    }

    private File trainingFile(TTDataSet data) throws IOException {
        try {
            GenericTTDataSet gds = (GenericTTDataSet) data;
            CSVDataSource csv = (CSVDataSource) gds.getTrainingData();
            if (",".equals(csv.getDelimiter())) {
                File file = csv.getFile();
                logger.debug("using training file {}", file);
                return file;
            }
        } catch (ClassCastException e) {
            /* No-op - this is fine, we will make a file. */
        }
        File file = makeCSV(data.getTrainingDAO(), getFile("train.csv"), true);
        logger.debug("wrote training file {}", file);
        return file;
    }

    private File testFile(TTDataSet data) throws IOException {
        File file = makeCSV(data.getTestDAO(), getFile("test.csv"), false);
        logger.debug("wrote test file {}", file);
        return file;
    }

    private File makeCSV(EventDAO dao, File file, boolean writeRatings) throws IOException {
        // TODO Make this not re-copy data unnecessarily
        Object[] row = new Object[writeRatings ? 3 : 2];
        TableWriter table = CSVWriter.open(file, null);
        try {
            Cursor<Rating> ratings = dao.streamEvents(Rating.class);
            try {
                for (Rating r: ratings.fast()) {
                    Preference p = r.getPreference();
                    if (p != null) {
                        row[0] = r.getUserId();
                        row[1] = r.getItemId();
                        if (writeRatings) {
                            row[2] = p.getValue();
                        }
                        table.writeRow(row);
                    }
                }
            } finally {
                ratings.close();
            }
        } finally {
            table.close();
        }
        return file;
    }

    private Long2ObjectMap<SparseVector> readPredictions(File predFile) throws FileNotFoundException, RecommenderBuildException {
        Long2ObjectMap<Long2DoubleMap> data = new Long2ObjectOpenHashMap<Long2DoubleMap>();
        Cursor<String[]> cursor = new DelimitedTextCursor(predFile, algorithm.getOutputDelimiter());
        try {
            int n = 0;
            for (String[] row: cursor) {
                n++;
                if (row.length == 0 || row.length == 1 && row[0].equals("")) {
                    continue;
                }
                if (row.length < 3) {
                    logger.error("predictions line {}: invalid row {}", n,
                                 StringUtils.join(row, ","));
                    throw new RecommenderBuildException("invalid prediction row");
                }
                long uid = Long.parseLong(row[0]);
                long iid = Long.parseLong(row[1]);
                double pred = Double.parseDouble(row[2]);
                Long2DoubleMap user = data.get(uid);
                if (user == null) {
                    user = new Long2DoubleOpenHashMap();
                    data.put(uid, user);
                }
                user.put(iid, pred);
            }
        } finally {
            cursor.close();
        }
        Long2ObjectMap<SparseVector> vectors = new Long2ObjectOpenHashMap<SparseVector>(data.size());
        for (Long2ObjectMap.Entry<Long2DoubleMap> entry: data.long2ObjectEntrySet()) {
            vectors.put(entry.getLongKey(), ImmutableSparseVector.create(entry.getValue()));
        }
        return vectors;
    }

    /**
     * External algorithmInfo implementation of TestUser.
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    class TestUserImpl extends AbstractTestUser {
        private final long userId;

        public TestUserImpl(long uid) {
            userId = uid;
        }

        @Override
        public UserHistory<Event> getTrainHistory() {
            UserHistory<Event> events = userTrainingEvents.getEventsForUser(userId);
            if(events == null){
                return History.forUser(userId); //Creates an empty history for this particular user.
            } else {
                return events;
            }
        }

        @Override
        public UserHistory<Event> getTestHistory() {
            return userTestEvents.getEventsForUser(userId);
        }

        @Override
        public SparseVector getPredictions() {
            return userPredictions.get(userId);
        }

        @Override
        public List<ScoredId> getRecommendations(int n, ItemSelector candSel, ItemSelector exclSel) {
            return null;
        }

        @Override
        public Recommender getRecommender() {
            return null;
        }
    }
}
