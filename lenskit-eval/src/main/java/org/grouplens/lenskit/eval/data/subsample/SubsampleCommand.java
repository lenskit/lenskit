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

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.data.CSVDataSourceCommand;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.grouplens.lenskit.util.tablewriter.CSVWriter;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
/**
 * The command to build and run a Subsample on the data source file and output the partition files
 *
 * @author Lingfei He<Lingfei@cs.umn.edu>
 */
public class SubsampleCommand extends AbstractCommand<DataSource> {
    private static final Logger logger = LoggerFactory.getLogger(SubsampleCommand.class);

    private static final Random random = new Random();

    private DataSource source;
    private double subsampleFraction = 0.1;
    private double randomProportion;
    private String output;
    private boolean isForced;
    
    public SubsampleCommand() {
        super("Subsample");
    }

    public SubsampleCommand(String name) {
        super();
        if (name != null) {
            setName(name);
        }
    }
    
    /**
     * Set the fraction of subsample to generate.
     *
     * @param fraction The number of percentage
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleCommand setSubsampleFraction(double fraction) {
        if(fraction >= 0 && fraction <= 1){
            subsampleFraction = fraction;
        }else {
            subsampleFraction = 0.1;
        }
        return this;
    }
    
    /**
     * Set the pattern for the out putsubsample File.
     *
     * @param pat The subsample file name pattern.
     * @return The SubsampleCommand object  (for chaining)
     * @see #setTrain(String)
     */
    public SubsampleCommand setOutput(String pat) {
        output = pat;
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
     * Set the force running option of the command. The subsample will be forced to
     * ran with the isForced set to true regardless of whether the partition files
     * are up to date.
     *
     * @param force The force to run option
     * @return The SubsampleCommand object  (for chaining)
     */
    public SubsampleCommand setForce(boolean force) {
        isForced = force;
        return this;
    }

    /**
     * Get the visible name of this subsample file.
     *
     * @return The name of the subsample file.
     */
    @Override
    public String getName() {
        if (name.equals("Subsample")) {
            return source.getName();
        } else {
            return name;
        }
    }
    
    /**
     * Get the output name of subsample file
     * 
     * @return The output name of the subsample file
     */
    public String getOutput() {
        if (output == null) {
            output = name + ".subsample.csv";
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
    public double getSubsampleFraction() {
        return subsampleFraction;
    }

    /**
     * Set the force running option of the command. The subsample will be forced to
     * ran with the isForced set to true regardless of whether the files
     * are up to date.
     *
     * @param force The force to run option
     * @return The SubsampleCommand object  (for chaining)
     */
    public boolean getForce() {
        return isForced || getConfig().force();
    }
    
    /**
     * Use CSVDataSourceCommand to generate output DataSource file
     * 
     * @return DataSource The subsample DataSource file
     */
    public DataSource getSubsampleFile() {
        File subsampleFile = new File(output);
        CSVDataSourceCommand sampleCommand = new CSVDataSourceCommand()
                .setDomain(source.getPreferenceDomain())
                .setFile(subsampleFile);
        return sampleCommand.call();
    }
    
    /**
     * Initialize the output file name before call subsampleCommand
     * 
     * @return The SubsampleCommand object  (for chaining
     */
    private SubsampleCommand initialize() {
        if (output == null) {
            output = name + ".subsample.csv";
        }
        return this;
    }
    
    /**
     * Run the Subsample command. Write the subsample files to the disk by reading in the source file.
     *
     * @return DataSource The subsample DataSource file
     * @throws org.grouplens.lenskit.eval.CommandException
     *
     */
    @Override
    public DataSource call() throws CommandException {
        this.initialize();
        if (!getForce()) {
            UpToDateChecker check = new UpToDateChecker();
            check.addInput(source.lastModified());
            File subsampleFile = new File(output);
            check.addOutput(subsampleFile);
            if (check.isUpToDate()) {
                logger.info("subsample {} up to date", getName());
                return getSubsampleFile();
            }
        }
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        try {
            logger.info("Set subsample data source {} to {} proportion",
                        getName(), subsampleFraction);
            createFile(daoSnap);
        } finally {
            daoSnap.close();
        }
        return getSubsampleFile();
    }

    /**
     * Randomly create the subsample file from the DAO using shuffle method.
     *
     * @param dao The DAO of the data source file
     * @throws org.grouplens.lenskit.eval.CommandException
     *          Any error
     */
    protected void createFile(DataAccessObject dao ) throws CommandException {
        File subsampleFile = new File(output);
        TableWriter subsampleWriter = null;
        try {
            try {
                subsampleWriter = CSVWriter.open(subsampleFile, null);
            } catch (IOException e) {
                throw new CommandException("Error creating subsample file writer", e);
            }
            Cursor<Rating> events = dao.getEvents(Rating.class);
            List<Rating> ratings = Cursors.makeList(events);
            try {
                final int n = ratings.size();
                randomProportion = subsampleFraction * n;
                final int m = (int)randomProportion;
                int randomNumber;
                for (int i = 0; i < m; i++) {
                    randomNumber = random.nextInt(n-1-i) + i;
                    ratings.set(i, ratings.set(randomNumber, ratings.get(i)));
                        
                    writeRating(subsampleWriter, ratings.get(i));     
                }
            } catch (IOException e) {
                throw new CommandException("Error writing to the train test files", e);
            } 
        } finally {
            LKFileUtils.close(logger, subsampleWriter);
        }
    }

    /**
     * Writing a rating event to the file using table writer
     *
     * @param writer The table writer to output the rating
     * @param rating The rating event to output
     * @throws IOException The writer IO error
     */
    protected void writeRating(TableWriter writer, Rating rating) throws IOException {
        String[] row = new String[4];
        row[0] = Long.toString(rating.getUserId());
        row[1] = Long.toString(rating.getItemId());
        Preference pref = rating.getPreference();
        row[2] = pref != null ? Double.toString(pref.getValue()) : "NaN";
        row[3] = Long.toString(rating.getTimestamp());
        writer.writeRow(row);
    }
}
