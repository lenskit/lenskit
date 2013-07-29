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
package org.grouplens.lenskit.eval.algorithm;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.script.BuiltBy;
import org.grouplens.lenskit.eval.data.CSVDataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.DelimitedTextCursor;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * An algorithm instance backed by an external program.
 */
@BuiltBy(ExternalAlgorithmInstanceBuilder.class)
public class ExternalAlgorithmInstance implements AlgorithmInstance {
    private final Logger logger = LoggerFactory.getLogger(ExternalAlgorithmInstance.class);

    private final String name;
    private final Map<String, Object> attributes;
    private final List<String> command;
    private final File workDir;
    private final String outputDelimiter;

    public ExternalAlgorithmInstance(String name, Map<String, Object> attrs,
                                     List<String> cmd, File dir, String delim) {
        this.name = name;
        attributes = attrs;
        command = cmd;
        workDir = dir;
        outputDelimiter = delim;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public List<String> getCommand() {
        return command;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExternalAlgorithm(")
          .append(getName())
          .append(")");
        if (!attributes.isEmpty()) {
            sb.append("[");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(sb, attributes);
            sb.append("]");
        }
        return sb.toString();
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
        File file = makeCSV(data.getTrainingDAO(), getName() + ".train.csv", true);
        logger.debug("wrote training file {}", file);
        return file;
    }

    private File testFile(TTDataSet data) throws IOException {
        File file = makeCSV(data.getTestDAO(), getName() + ".test.csv", false);
        logger.debug("wrote test file {}", file);
        return file;
    }

    private File makeCSV(EventDAO dao, String fn, boolean writeRatings) throws IOException {
        // TODO Make this not re-copy data unnecessarily
        File file = new File(workDir, fn);
        Object[] row = new Object[writeRatings ? 3 : 2];
        TableWriter table = CSVWriter.open(file, null);
        try {
            Cursor<Rating> ratings = dao.streamEvents(Rating.class);
            try {
                for (Rating r: ratings) {
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

    @Override
    public RecommenderInstance makeTestableRecommender(TTDataSet data, Provider<? extends PreferenceSnapshot> snapshot,
                                                       ExecutionInfo info) throws RecommenderBuildException {
        final File train;
        try {
            train = trainingFile(data);
        } catch (IOException e) {
            throw new RecommenderBuildException("error preparing training file", e);
        }
        final File test;
        try {
            test = testFile(data);
        } catch (IOException e) {
            throw new RecommenderBuildException("error preparing test file", e);
        }
        final File output = new File(workDir,
                                     String.format("%s-%s.predictions.csv", getName(), data.getName()));
        List<String> args = Lists.transform(command, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                if (input == null) {
                    throw new IllegalArgumentException("cannot have null command element");
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
                                       .directory(workDir)
                                       .start();
        } catch (IOException e) {
            throw new RecommenderBuildException("error creating process", e);
        }
        Thread listen = new ProcessErrorHandler(proc.getErrorStream());
        listen.run();

        int result = -1;
        boolean done = false;
        while (!done) {
            try {
                result = proc.waitFor();
                done = true;
            } catch (InterruptedException e) {
                /* try again */
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

        return new RecInstance(data.getTrainingData().getUserEventDAO(), vectors);
    }

    private Long2ObjectMap<SparseVector> readPredictions(File predFile) throws FileNotFoundException, RecommenderBuildException {
        Long2ObjectMap<Long2DoubleMap> data = new Long2ObjectOpenHashMap<Long2DoubleMap>();
        Cursor<String[]> cursor = new DelimitedTextCursor(predFile, outputDelimiter);
        try {
            for (String[] row: cursor) {
                if (row.length < 3) {
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
        for (Long2ObjectMap.Entry<Long2DoubleMap> entry: CollectionUtils.fast(data.long2ObjectEntrySet())) {
            vectors.put(entry.getLongKey(), new ImmutableSparseVector(entry.getValue()));
            entry.setValue(null);
        }
        return vectors;
    }

    private static class RecInstance implements RecommenderInstance {
        private final UserEventDAO dao;
        private final Long2ObjectMap<SparseVector> vectors;

        public RecInstance(UserEventDAO dao, Long2ObjectMap<SparseVector> vs) {
            this.dao = dao;
            vectors = vs;
        }

        @Override
        public UserEventDAO getUserEventDAO() {
            return dao;
        }

        @Override
        public SparseVector getPredictions(long uid, LongSet testItems) {
            return vectors.get(uid);
        }

        @Override
        public List<ScoredId> getRecommendations(long uid, LongSet testItems, int n) {
            return null;
        }

        @Override
        public Recommender getRecommender() {
            return null;
        }
    }

    private class ProcessErrorHandler extends Thread {
        private final BufferedReader error;

        public ProcessErrorHandler(InputStream err) {
            super("external");
            setDaemon(true);
            error = new BufferedReader(new InputStreamReader(err));
        }

        @Override
        public void run() {
            String line;
            try {
                while ((line = error.readLine()) != null) {
                    logger.debug("external: " + line);
                }
            } catch (IOException e) {
                logger.error("IO error reading error stream", e);
            }
        }
    }
}
