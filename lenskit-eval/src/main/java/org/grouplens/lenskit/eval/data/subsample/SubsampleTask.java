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
package org.grouplens.lenskit.eval.data.subsample;

import com.google.common.io.Closer;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.RatingWriter;
import org.grouplens.lenskit.eval.data.RatingWriters;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
/**
 * The command to build and run a Subsample on the data source file and output the partition files
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SubsampleTask extends AbstractTask<DataSource> {
    private static final Logger logger = LoggerFactory.getLogger(SubsampleTask.class);

    private DataSource source;
    private double subsampleFraction = 0.1;
    private SubsampleMode mode = SubsampleMode.RATING;
    
    @Nullable
    private File output;
    
    public SubsampleTask() {
        super("subsample");
    }

    public SubsampleTask(String name) {
        super(name);
    }

    /**
     * Set the fraction of subsample to generate.
     *
     * @param fraction The fraction of ratings to keep.
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleTask setFraction(double fraction) throws IllegalArgumentException {
        if (fraction >= 0 && fraction <= 1) {
            subsampleFraction = fraction;
        } else {
            String msg = String.format("fraction %f not in range [0,1]", fraction);
            throw new IllegalArgumentException(msg);
        }
        return this;
    }
    
    /**
     * Set the output file name.
     *
     * @param name The name of the output file.
     * @return The subsample task (for chaining).
     * @see #setOutput(java.io.File)
     */
    public SubsampleTask setOutput(String name) {
        return setOutput(new File(name));
    }

    /**
     * Set the output file for the sampled data.
     * @param out The output file.
     * @return The task (for chaining).
     */
    public SubsampleTask setOutput(File out) {
        output = out;
        return this;
    }
    
    /**
     * Set the input data source.
     *
     * @param source The data source to use.
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleTask setSource(DataSource source) {
        this.source = source;
        return this;
    }
    
    /**
     * Set the mode of the subsample, it is user, item or rating.
     * 
     * @param mode The mode of the output.
     * @return The SubsampleCommand object  (for chaining).
     */
    public SubsampleTask setMode(SubsampleMode mode) {
        this.mode = mode;
        return this;
    }
    
    /**
     * Get the output name of subsample file.
     * 
     * @return The output name of the subsample file.
     */
    public File getOutput() {
        if (output == null) {
            return new File(getName() + ".subsample.csv");
        }
        return output;
    }

    /**
     * Get the data source backing this subsample manager.
     *
     * @return The underlying data source.
     */
    public DataSource getSource() {
        return source;
    }

    /**
     * Get the size of file.
     *
     * @return The number of size in subsample file.
     */
    public double getFraction() {
        return subsampleFraction;
    }

    /**
     * Get the mode of the subsample.
     * 
     * @return The mode of the subsample
     */
    public SubsampleMode getMode() {
        return mode;
    }
    
    /**
     * Use CSVDataSourceCommand to generate output DataSource file
     * 
     * @return DataSource The subsample DataSource file
     */
    private DataSource makeDataSource() {
        CSVDataSourceBuilder bld = new CSVDataSourceBuilder()
                .setDomain(source.getPreferenceDomain())
                .setFile(getOutput());
        return bld.build();
    }
    
    /**
     * Run the Subsample command.
     *
     * @return DataSource The subsample DataSource file
     * @throws org.grouplens.lenskit.eval.TaskExecutionException
     *
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    @Override
    public DataSource perform() throws TaskExecutionException {
        UpToDateChecker check = new UpToDateChecker();
        check.addInput(source.lastModified());
        File subsampleFile = getOutput();
        check.addOutput(subsampleFile);
        if (check.isUpToDate()) {
            logger.info("subsample {} up to date", getName());
            return makeDataSource();
        }
        try {
            logger.info("sampling {} of {}",
                        subsampleFraction, source.getName());
            Closer closer = Closer.create();
            RatingWriter subsampleWriter = closer.register(RatingWriters.csv(subsampleFile));
            try {
                mode.doSample(source, subsampleWriter, subsampleFraction, getProject().getRandom());
            } catch (Throwable th) {
                throw closer.rethrow(th);
            } finally {
                closer.close();
            }
        } catch (IOException e) {
            throw new TaskExecutionException("Error writing output file", e);
        }
        return makeDataSource();
    }
}
