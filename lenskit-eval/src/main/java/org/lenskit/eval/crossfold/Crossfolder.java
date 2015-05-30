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
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.event.Rating;
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
    private boolean skipIfUpToDate = false;
    private CrossfoldMethod method = CrossfoldMethods.partitionUsers(new RandomOrder<Rating>(), new HoldoutNPartition<Rating>(10));
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
    public Crossfolder setPartitionCount(int partition) {
        partitionCount = partition;
        return this;
    }

    /**
     * Get the partition count.
     * @return The number of partitions that will be generated.
     */
    public int getPartitionCount() {
        return partitionCount;
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
     * Set the method to be used by the crossfolder.
     * @param meth The method to use.
     * @return The crossfolder (for chaining).
     */
    public Crossfolder setMethod(CrossfoldMethod meth) {
        method = meth;
        return this;
    }

    /**
     * Get the method to be used for crossfolding.
     * @return The configured crossfold method.
     */
    public CrossfoldMethod getMethod() {
        return method;
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

    List<Path> getTrainingFiles() {
        return getFileList("part%02d.train." + outputFormat.getSuffix());
    }

    List<Path> getTestFiles() {
        return getFileList("part%02d.test." + outputFormat.getSuffix());
    }

    List<Path> getSpecFiles() {
        return getFileList("part%02d.json");
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
        Files.createDirectories(outputDir);
        try (CrossfoldOutput out = new CrossfoldOutput(this, rng)) {
            method.crossfold(source, out);
        }

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

    RatingWriter openWriter(Path file) throws IOException {
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
