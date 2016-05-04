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
package org.lenskit.eval.temporal;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.ModelDisposition;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.SortOrder;
import org.lenskit.data.packed.BinaryRatingDAO;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStreams;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;
import org.lenskit.util.table.writer.CSVWriter;
import org.lenskit.util.table.writer.TableWriter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.sqrt;

public class TemporalEvaluator {
    private BinaryRatingDAO dataSource;
    private AlgorithmInstance algorithm;
    private File predictOutputFile;
    private Long rebuildPeriod;
    private Integer listSize;
    private Random rng;

    public TemporalEvaluator() {
        rebuildPeriod = 86400L;
        listSize = 10;
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
    public TemporalEvaluator setDataSource(BinaryRatingDAO dao) {
        dataSource = dao;
        return this;
    }

    /**
     * @param file The file contains the ratings input data
     * @return itself
     */
    public TemporalEvaluator setDataSource(File file) throws IOException {
        dataSource = BinaryRatingDAO.open(file);
        return this;
    }

    /**
     * @param file The file set as the output of the command
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setPredictOutputFile(File file) {
        predictOutputFile = file;
        return this;
    }

    /**
     * *
     *
     * @param file  The file set as the output of the command
     * @param cMode Compression Mode
     * @return Itself for  method chaining
     */

    //TODO Set compression mode in file
    public TemporalEvaluator setPredictOutputFile(File file, CompressionMode cMode) {
        predictOutputFile = file;
        return this;
    }

    /**
     * @param time The time to rebuild
     * @param unit Unit of time set
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setRebuildPeriod(Long time, TimeUnit unit) {
        rebuildPeriod = unit.toSeconds(time);
        return this;
    }

    /**
     * @param seconds default rebuild period in seconds
     * @return Itself for  method chaining
     */
    public TemporalEvaluator setRebuildPeriod(Long seconds) {
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
    public File getPredictOutputFile() {
        return predictOutputFile;
    }

    /**
     * @return Returns rebuild period
     */
    public Long getRebuildPeriod() {
        return rebuildPeriod;
    }

    /**
     * @return size of recommendation list
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * During the evaluation, it will replay the ratings, try to predict each one, and
     * write the prediction, TARMSE and the rating to the output file
     */

    public void execute() throws IOException, RecommenderBuildException {
        Preconditions.checkState(algorithm != null, "no algorithm specified");
        Preconditions.checkState(dataSource != null, "no input data specified");
        Preconditions.checkState(predictOutputFile != null, "no output file specified");
        //Builds file layout
        TableLayoutBuilder tlb = new TableLayoutBuilder();
        //file headers
        tlb.addColumn("User")
           .addColumn("Item")
           .addColumn("Rating")
           .addColumn("Timestamp")
           .addColumn("Prediction")
           .addColumn("TARMSE")
           .addColumn("ModelAge")
           .addColumn("Rank")
           .addColumn("Rebuilds");

        TableLayout tl = tlb.build();

        //Initialize recommender engine and recommender
        LenskitRecommenderEngine lre = null;
        Recommender recommender = null;

        //Start try block -- will try to write output on file
        try (TableWriter tableWriter = CSVWriter.open(predictOutputFile, tl, CompressionMode.AUTO)) {
            List<Rating> ratings = ObjectStreams.makeList(dataSource.streamEvents(Rating.class, SortOrder.TIMESTAMP));
            BinaryRatingDAO limitedDao = dataSource.createWindowedView(0);

            //Initialize local variables, will use to calculate RMSE
            double sse = 0;
            int n = 0;
            // Initialize build parameters
            long buildTime = 0L;
            int buildsCount = 0;

            //Loop through ratings
            for (Rating r : ratings) {
                if (r.getTimestamp() > 0 && limitedDao.getLimitTimestamp() < r.getTimestamp()) {
                    limitedDao = dataSource.createWindowedView(r.getTimestamp());
                    LenskitConfiguration config = new LenskitConfiguration();
                    config.addComponent(limitedDao);

                    //rebuild recommender system if its older then rebuild period set or null
                    if ((r.getTimestamp() - buildTime >= rebuildPeriod) || lre == null) {
                        buildTime = r.getTimestamp();
                        lre = LenskitRecommenderEngine.newBuilder()
                                                      .addConfiguration(algorithm.getConfigurations().get(0))
                                                      .addConfiguration(config, ModelDisposition.EXCLUDED)
                                                      .build();
                        buildsCount++;
                    }
                    if (recommender != null) {
                        recommender.close();
                    }
                    recommender = lre.createRecommender(config);
                }

                 //get prediction score
                double predict = Double.NaN;
                Result predictionResult = recommender.getRatingPredictor().predict(r.getUserId(), r.getItemId());

                //check result to avoid null exception
                if (predictionResult != null) {
                    predict = predictionResult.getScore();
                }
                /***calculate Time Averaged RMSE***/
                double rmse = 0.0;
                if (!Double.isNaN(predict)) {
                    double err = predict - r.getValue();
                    sse += err * err;
                    n++;
                    rmse = sqrt(sse / n);
                }

                /***calculate recommendation rank***/
                // item rank
                int rank = -1; //rank < 0 indicates item not found
                /* set of all items in limited DAO */
                LongSet itemsInDao = new LongOpenHashSet(limitedDao.getItemIds());
                /* set of all items rated by user */
                LongSet itemsByUser = new LongOpenHashSet();
                /* set of candidates that includes current item +
                   listsize-1 random values from (items from dao - items rated by user) */
                LongSet candidates = new LongOpenHashSet();

                //Check if events for users exists to avoid NULL exception
                if (limitedDao.getEventsForUser(r.getUserId()) != null) {
                    itemsByUser.addAll(limitedDao.getEventsForUser(r.getUserId()).itemSet());
                    candidates.addAll(LongUtils.randomSubset(itemsInDao, listSize - 1, itemsByUser, rng));
                }

                //Check size to avoid exception
                if (candidates.size() > 0) {
                    //if current item is removed from candidates, add it again
                    if (!candidates.contains(r.getItemId())) {
                        candidates.add(r.getItemId());
                    }
                    // get list of recommendations
                    List<Long> recs = recommender.getItemRecommender().recommend(r.getUserId(), listSize, candidates, itemsByUser);
                    rank = recs.indexOf(r.getItemId());
                 }
                //increment index to get correct rank
                rank++;
                /**writes the Prediction Score, Rank and TARMSE on file.**/
                tableWriter.writeRow(r.getUserId(), r.getItemId(), r.getValue(), r.getTimestamp(),
                                     predict, rmse, r.getTimestamp() - buildTime, rank, buildsCount);
            }//ratings loop end here
        } finally {
            if (recommender != null) {
                recommender.close();
            }
        }
    }
}

