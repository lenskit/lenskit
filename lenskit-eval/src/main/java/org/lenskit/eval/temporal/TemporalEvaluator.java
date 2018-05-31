/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.temporal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.ratings.Rating;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;
import org.lenskit.util.table.writer.CSVWriter;
import org.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.sqrt;

public class TemporalEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(TemporalEvaluator.class);
    @Nonnull
    private Random rng;
    private AlgorithmInstance algorithm;
    private DataAccessObject dataSource;
    private File outputFile;
    private File extendedOutputFile;
    private long rebuildPeriod;
    private int listSize;

    public TemporalEvaluator() {
        setRebuildPeriod(24, TimeUnit.HOURS);
        setListSize(10);
        rng = new Random();
    }

    /**
     * Adds an algorithmInfo
     *
     * @param algo The algorithmInfo added
     * @return Itself to allow  chaining
     */
    public TemporalEvaluator setAlgorithm(AlgorithmInstance algo) {
        algorithm = algo;
        return this;
    }

    /**
     * An algorithm instance constructed with a name and Lenskit configuration
     *
     * @param name   Name of algorithm instance
     * @param config Lenskit configuration
     */
    public TemporalEvaluator setAlgorithm(String name, LenskitConfiguration config) {
        algorithm = new AlgorithmInstance(name, config);
        return this;
    }


    /**
     * @param dao The datasource to be added to the command.
     * @return Itself to allow for  method chaining.
     */
    public TemporalEvaluator setDataSource(DataAccessObject dao) {
        dataSource = dao;
        return this;
    }

    /**
     * @param file The file contains the ratings input data
     * @return itself
     */
    public TemporalEvaluator setDataSource(File file) throws IOException {
        dataSource = StaticDataSource.load(file.toPath()).get();
        return this;
    }

    /**
     * @param file The file set as the output of the command
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setOutputFile(File file) {
        outputFile = file;
        return this;
    }

    /**
     * Get the output file for extended output (lines of JSON).
     * @return the output file.
     */
    @Nullable
    public Path getExtendedOutputFile() {
        return extendedOutputFile.toPath();
    }

    /**
     * Set the output file for extended output (lines of JSON).
     * @param file The output file name.
     * @return The evaluator (for chaining).
     */
    public TemporalEvaluator setExtendedOutputFile(@Nullable File file) {
        extendedOutputFile = file;
        return setExtendedOutputFile(file != null ? file.toPath() : null);
    }

    /**
     * Set the output file for extended output (lines of JSON).
     * @param file The output file name.
     * @return The evaluator (for chaining).
     */
    public TemporalEvaluator setExtendedOutputFile(@Nullable Path file) {
        extendedOutputFile = file != null ? file.toFile() : null;
        return this;
    }

    /**
     * @param time The time to rebuild
     * @param unit Unit of time set
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setRebuildPeriod(long time, TimeUnit unit) {
        return setRebuildPeriod(unit.toSeconds(time));
    }

    /**
     * @param seconds default rebuild period in seconds
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setRebuildPeriod(long seconds) {
        rebuildPeriod = seconds;
        return this;
    }

    /**
     * sets the size of recommendationss list size
     *
     * @param lSize size of list to be set
     * @return returns itself
     */
    public TemporalEvaluator setListSize(int lSize) {
        listSize = lSize;
        return this;
    }

    /**
     * @return Returns prediction output file
     */
    public Path getOutputFile() {
        return outputFile.toPath();
    }

    /**
     * @return Returns rebuild period
     */
    public long getRebuildPeriod() {
        return rebuildPeriod;
    }

    /**
     * @return size of recommendation list
     */
    public int getListSize() {
        return listSize;
    }

    private void loadInputs() throws IOException {
        Preconditions.checkState(dataSource != null, "no input data specified");
        Preconditions.checkState(algorithm != null,
                                 "no algorithm specified");
    }

    /**
     * During the evaluation, it will replay the ratings, try to predict each one, and
     * write the prediction, TARMSE and the rating to the output file
     */
    public void execute() throws IOException, RecommenderBuildException {
        loadInputs();

        //Initialize recommender engine and recommender
        LenskitRecommenderEngine lre = null;
        Recommender recommender = null;

        //Start try block -- will try to write output on file
        try (TableWriter tableWriter = openOutput();
             SequenceWriter extWriter = openExtendedOutput()) {
            // FIXME Don't keep this whole list in (second) memory.
            List<Rating> ratings = dataSource.query(Rating.class)
                                             .orderBy(CommonAttributes.TIMESTAMP)
                                             .get();

            DataAccessObject limitedDao = StaticDataSource.fromList(Collections.<Entity>emptyList()).get();
            long limitTimestamp = 0;

            //Initialize local variables, will use to calculate RMSE
            double sse = 0;
            int n = 0;
            // Initialize build parameters
            long buildTime = 0L;
            int buildsCount = 0;
            int ratingsSinceLastBuild = 0;

            //Loop through ratings
            ListIterator<Rating> riter = ratings.listIterator();
            while (riter.hasNext()) {
                int ridx = riter.nextIndex();
                Rating r = riter.next();
                Map<String,Object> json = new HashMap<>();
                json.put("userId", r.getUserId());
                json.put("itemId", r.getItemId());
                json.put("timestamp", r.getTimestamp());
                json.put("rating", r.getValue());

                if (recommender == null || (r.getTimestamp() > 0 && limitTimestamp < r.getTimestamp())) {
                    limitedDao = StaticDataSource.fromList(ratings.subList(0, ridx)).get();

                    //rebuild recommender system if its older then rebuild period set or null
                    if ((r.getTimestamp() - buildTime >= rebuildPeriod) || lre == null) {
                        buildTime = r.getTimestamp();
                        buildsCount++;

                        logger.info("building model {} at time {}, {} ratings since last build",
                                    buildsCount, buildTime, ratingsSinceLastBuild);

                        Stopwatch timer = Stopwatch.createStarted();
                        lre = LenskitRecommenderEngine.newBuilder()
                                                      .addConfiguration(algorithm.getConfigurations().get(0))
                                                      .build(limitedDao);
                        timer.stop();
                        logger.info("built model {} in {}", buildsCount, timer);

                        ratingsSinceLastBuild = 0;
                    }
                    if (recommender != null) {
                        recommender.close();
                    }
                    recommender = lre.createRecommender(limitedDao);
                }
                ratingsSinceLastBuild += 1;

                json.put("modelAge", r.getTimestamp() - buildTime);

                // get rating prediction if available
                Double predict = null;
                RatingPredictor predictor = recommender.getRatingPredictor();
                Result predictionResult = null;
                if (predictor != null) {
                    predictionResult = predictor.predict(r.getUserId(), r.getItemId());
                }

                if (predictionResult != null) {
                    predict = predictionResult.getScore();
                    logger.debug("predicted {} for rating {}", predict, r);
                    json.put("prediction", predict);
                } else {
                    json.put("prediction", null);
                }

                /***calculate Time Averaged RMSE***/
                double rmse = 0.0;
                if (predict != null && !Double.isNaN(predict)) {
                    double err = predict - r.getValue();
                    sse += err * err;
                    n++;
                    rmse = sqrt(sse / n);
                }

                // Compute recommendations
                Integer rank = null;
                ItemRecommender irec = recommender.getItemRecommender();
                if (irec != null) {
                    rank = getRecommendationRank(limitedDao, r, json, irec);

                }

                /**writes the Prediction Score, Rank and TARMSE on file.**/
                tableWriter.writeRow(r.getUserId(), r.getItemId(), r.getValue(), r.getTimestamp(),
                                     predict, rmse, r.getTimestamp() - buildTime, rank, buildsCount);
                if (extWriter != null) {
                    extWriter.write(json);
                }
            } // loop ratings

        } finally {
            if (recommender != null) {
                recommender.close();
            }
        }
    }

    /**
     * Get the rank of the recommended item.
     * @param dao The limited DAO.
     * @param rating The rating.
     * @param json The JSON object being built.
     * @param irec The item recommender.
     * @return The rank, or `null` if the item is not recommended.
     */
    @Nullable
    private Integer getRecommendationRank(DataAccessObject dao, Rating rating, Map<String, Object> json, ItemRecommender irec) {
        Integer rank; /***calculate recommendation rank***/
                    /* set of candidates that includes current item +
                       listsize-1 random values from (items from dao - items rated by user) */
        LongSet candidates = new LongOpenHashSet();
                    /* Users *not* to include in candidate set */
        LongSet excludes = new LongOpenHashSet();
        // include the target item...
        candidates.add(rating.getItemId());
        // .. and exlude it from being added again
        excludes.add(rating.getItemId());

        //Check if events for users exists to avoid NULL exception
        excludes.addAll(dao.query(Rating.class)
                           .withAttribute(CommonAttributes.USER_ID, rating.getUserId())
                           .valueSet(CommonAttributes.ITEM_ID));

        // Add a random set of decoy items
        candidates.addAll(LongUtils.randomSubset(dao.getEntityIds(CommonTypes.ITEM),
                                                 listSize - 1, excludes, rng));

        // get list of recommendations
        List<Long> recs = irec.recommend(rating.getUserId(), listSize, candidates, null);
        json.put("recommendations", recs);
        rank = recs.indexOf(rating.getItemId());
        if (rank >= 0) {
            //increment index to get correct rank
            rank++;
        } else {
            rank = null;
        }
        return rank;
    }

    @Nullable
    private TableWriter openOutput() throws IOException {
        TableLayoutBuilder tlb = new TableLayoutBuilder();

        tlb.addColumn("User")
           .addColumn("Item")
           .addColumn("Rating")
           .addColumn("Timestamp")
           .addColumn("Prediction")
           .addColumn("TARMSE")
           .addColumn("ModelAge")
           .addColumn("Rank")
           .addColumn("Rebuilds");

        TableLayout layout = tlb.build();

        return CSVWriter.open(outputFile, layout, CompressionMode.AUTO);
    }

    @Nullable
    private SequenceWriter openExtendedOutput() throws IOException {
        if (extendedOutputFile == null) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter w = mapper.writer().withRootValueSeparator(System.lineSeparator());
        return w.writeValues(extendedOutputFile);
    }
}

