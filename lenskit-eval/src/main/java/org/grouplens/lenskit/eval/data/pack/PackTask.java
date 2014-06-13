/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data.pack;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingPacker;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.CSVDataSource;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.io.StagedWrite;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Pack data sets.  This task returns a list of the data sets.  It can pack both data sources
 * and train-test data sets, in which case it packs the test and training data.  It returns the
 * packed data sets.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackTask extends AbstractTask<List<Object>> {
    private static final Logger logger = LoggerFactory.getLogger(PackTask.class);

    private List<TTDataSet> trainTestSets = Lists.newArrayList();
    private List<DataSource> dataSources = Lists.newArrayList();
    private Function<DataSource,File> packFileFunction = new DefaultOutputFunction();
    private EnumSet<BinaryFormatFlag> binaryFlags = EnumSet.of(BinaryFormatFlag.TIMESTAMPS);

    public void addDataset(TTDataSet data) {
        Preconditions.checkNotNull(data, "data source");
        trainTestSets.add(data);
    }

    public void addDataset(DataSource data) {
        Preconditions.checkNotNull(data, "data source");
        dataSources.add(data);
    }

    /**
     * Set whether to include timestamps.
     * @param include If {@code true} (the default), timestamps will be packed.
     */
    public void setIncludeTimestamps(boolean include) {
        if (include) {
            binaryFlags.add(BinaryFormatFlag.TIMESTAMPS);
        } else {
            binaryFlags.remove(BinaryFormatFlag.TIMESTAMPS);
        }
    }

    /**
     * Get whether to include timestamps.
     * @return Whether or not timestamps will be included.
     */
    public boolean getIncludeTimestamps() {
        return binaryFlags.contains(BinaryFormatFlag.TIMESTAMPS);
    }

    /**
     * Set the function to produce the default files for each data source.  The function will take
     * data sources and produce the files into which they should be packed.  The default function
     * packs CSV data sources into binary files next to their source files, and other data sources
     * into files with unique names in the {@code pack} directory.
     *
     * @param func The function.
     */
    public void setOutputFile(Function<DataSource, File> func) {
        Preconditions.checkNotNull(func, "output file function");
        packFileFunction = func;
    }

    @Override
    protected List<Object> perform() throws TaskExecutionException, InterruptedException {
        List<Object> results = Lists.newArrayList();
        for (TTDataSet data: trainTestSets) {
            // FIXME Pack the query data
            DataSource train = packDataSource(data.getTrainingData());
            DataSource test = packDataSource(data.getTestData());
            results.add(GenericTTDataSet.copyBuilder(data)
                                        .setTrain(train)
                                        .setTest(test)
                                        .build());
        }
        for (DataSource data: dataSources) {
            results.add(packDataSource(data));
        }
        return results;
    }

    private DataSource packDataSource(DataSource data) throws TaskExecutionException {
        File outFile = packFileFunction.apply(data);
        Preconditions.checkNotNull(outFile, "output file");
        assert outFile != null;

        PackedDataSource source = new PackedDataSource(data.getName(), outFile,
                                                       data.getPreferenceDomain());

        UpToDateChecker check = new UpToDateChecker();
        check.addInput(data.lastModified());
        check.addOutput(outFile);
        if (check.isUpToDate()) {
            logger.info("{} is up to date", outFile);
            return source;
        }

        logger.info("packing {} to {}", data, outFile);
        StagedWrite stage = StagedWrite.begin(outFile);
        try {
            BinaryRatingPacker packer = BinaryRatingPacker.open(stage.getStagingFile(), binaryFlags);
            try {
                Cursor<Rating> ratings = data.getEventDAO().streamEvents(Rating.class);
                try {
                    packer.writeRatings(ratings);
                } finally {
                    ratings.close();
                }
            } finally {
                packer.close();
            }
            stage.commit();
        } catch (IOException ex) {
            logger.error("error packing {}: {}", outFile, ex);
            throw new TaskExecutionException("error packing " + outFile, ex);
        } finally {
            stage.close();
        }

        return source;
    }

    private class DefaultOutputFunction implements Function<DataSource, File> {
        @Nullable
        @Override
        public File apply(@Nullable DataSource input) {
            assert input != null;
            if (input instanceof CSVDataSource) {
                CSVDataSource csv = (CSVDataSource) input;
                File file = csv.getFile();
                String name = file.getName();
                return new File(file.getParentFile(), name + ".pack");
            } else {
                File dir = new File(getProject().getConfig().getDataDir());
                dir = new File(dir, "packed");
                UUID uuid = UUID.randomUUID();
                return new File(dir, uuid.toString() + ".pack");
            }
        }
    }
}
