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

package org.grouplens.lenskit.eval.data.subsample;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.data.CSVDataSourceCommand;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.grouplens.lenskit.util.tablewriter.CSVWriter;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.grouplens.lenskit.eval.data.subsample.SubsampleMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
/**
 * The command to build and run a Subsample on the data source file and output the partition files
 *
 * @author Lingfei He<Lingfei@cs.umn.edu>
 */
public class SubsampleCommand extends AbstractCommand<DataSource> {
    private static final Logger logger = LoggerFactory.getLogger(SubsampleCommand.class);

    private DataSource source;
    private double subsampleFraction = 0.1;
    private SubsampleMode mode = SubsampleMode.RATING;
    
    @Nullable
    private File output;
    
    public SubsampleCommand() {
        super("Subsample");
    }

    public SubsampleCommand(String name) {
        super(name);
    }
    
    /**
     * Set the fraction of subsample to generate.
     *
     * @param fraction The fraction of ratings to keep.
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleCommand setFraction(double fraction) throws IllegalArgumentException {
        if (fraction >= 0 && fraction <= 1) {
            subsampleFraction = fraction;
        } else {
            String msg = String.format("fraction %f not in range [0,1]", fraction);
            throw new IllegalArgumentException(msg);
        }
        return this;
    }
    
    /**
     * Configure the output file name for the out put subsample File.
     *
     * @param pat The subsample file name pattern.
     * @return The SubsampleCommand object  (for chaining)
     * @see #setTrain(String)
     */
    public SubsampleCommand setOutput(String name) {
        output = new File(name);
        return this;
    }
    
    /**
     * Set the input data source.
     *
     * @param source The data source to use.
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleCommand setSource(DataSource source) {
        this.source = source;
        return this;
    }
    
    /**
     * Set the mode of the subsample, it is user, item or rating.
     * 
     * @param mode The mode of the output.
     * @return The SubsampleCommand object  (for chaining).
     */
    public SubsampleCommand  setMode(SubsampleMode mode) throws IllegalArgumentException {
        if (mode.equals(SubsampleMode.RATING)) {
            this.mode = mode;
            return this;
        } else if (mode.equals(SubsampleMode.USER)) {
            this.mode = mode;
            return this;
        } else if (mode.equals(SubsampleMode.ITEM)) {
            this.mode = mode;
            return this;
        } else {
            String msg = "The mode should be: Rating, Item or User";
            throw new IllegalArgumentException(msg);
        }
    }
    
    /**
     * Get the output name of subsample file.
     * 
     * @return The output name of the subsample file.
     */
    public File getOutput() {
        if (output == null) {
            return new File(name + ".subsample.csv");
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
        File subsampleFile = getOutput();
        CSVDataSourceCommand sampleCommand = new CSVDataSourceCommand()
                .setDomain(source.getPreferenceDomain())
                .setFile(subsampleFile);
        return sampleCommand.call();
    }
    
    /**
     * Run the Subsample command.
     *
     * @return DataSource The subsample DataSource file
     * @throws org.grouplens.lenskit.eval.CommandException
     *
     */
    @Override
    public DataSource call() throws CommandException {
        UpToDateChecker check = new UpToDateChecker();
        check.addInput(source.lastModified());
        File subsampleFile = getOutput();
        check.addOutput(subsampleFile);
        if (check.isUpToDate()) {
            logger.info("subsample {} up to date", getName());
            return makeDataSource();
        }
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        try {
            logger.info("sampling {} of {}",
                        subsampleFraction, source.getName());
            TableWriter subsampleWriter = CSVWriter.open(subsampleFile, null);
            try {
                mode.doSample(daoSnap, subsampleWriter, subsampleFraction);
            } finally {
                LKFileUtils.close(subsampleWriter);
            }
        } catch (IOException e) {
            throw new CommandException("Error writing output file", e);
        } finally {
            daoSnap.close();
        }
        return makeDataSource();
    }
}
