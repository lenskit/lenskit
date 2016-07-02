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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.source.CSVDataSourceBuilder;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.data.source.PackedDataSourceBuilder;
import org.grouplens.lenskit.data.source.TextDataSource;
import org.grouplens.lenskit.util.io.UpToDateChecker;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.EntitySource;
import org.lenskit.data.dao.file.StaticFileDAOProvider;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.output.RatingWriter;
import org.lenskit.data.output.RatingWriters;
import org.lenskit.data.packed.BinaryFormatFlag;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.DataSetBuilder;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.CrossfoldSpec;
import org.lenskit.specs.eval.DataSetSpec;
import org.lenskit.specs.eval.OutputFormat;
import org.lenskit.specs.eval.PartitionMethodSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The command to build and run a crossfold on the data source file and output the partition files
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Crossfolder {
    public static final String ITEM_FILE_NAME = "items.txt";

    private static final Logger logger = LoggerFactory.getLogger(Crossfolder.class);

    private Random rng;
    private String name;
    private DataSource source;
    private EntityType entityType = CommonTypes.RATING;
    private int partitionCount = 5;
    private Path outputDir;
    private OutputFormat outputFormat = OutputFormat.CSV;
    private boolean skipIfUpToDate = false;
    private CrossfoldMethod method = CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10));
    private boolean writeTimestamps = true;

    public Crossfolder() {
        this(null);
    }

    public Crossfolder(String n) {
        name = n;
        rng = new Random();
    }

    /**
     * Instantiate a crossfolder from a spec.
     * @param spec The crossfold spec.
     * @return The crossfolder.
     */
    public static Crossfolder fromSpec(CrossfoldSpec spec) {
        Crossfolder cf = new Crossfolder();
        cf.setName(spec.getName())
          .setPartitionCount(spec.getPartitionCount())
          .setWriteTimestamps(spec.getIncludeTimestamps())
          .setOutputFormat(spec.getOutputFormat())
          .setOutputDir(spec.getOutputDir());

        SortOrder order = null;
        HistoryPartitionMethod part = null;
        PartitionMethodSpec pm = spec.getUserPartitionMethod();
        if (pm != null) {
            logger.debug("configuring partition method");
            order = SortOrder.fromString(pm.getOrder());
            if (pm instanceof PartitionMethodSpec.Holdout) {
                int count = ((PartitionMethodSpec.Holdout) pm).getCount();
                logger.debug("configuring holdout of {}", count);
                part = HistoryPartitions.holdout(count);
            } else if (pm instanceof PartitionMethodSpec.HoldoutFraction) {
                double fraction = ((PartitionMethodSpec.HoldoutFraction) pm).getFraction();
                logger.debug("configuring fraction of {}", fraction);
                part = HistoryPartitions.holdoutFraction(fraction);
            } else if (pm instanceof PartitionMethodSpec.Retain) {
                int count = ((PartitionMethodSpec.Retain) pm).getCount();
                logger.debug("configuring retain of {}", count);
                part = HistoryPartitions.retain(count);
            } else {
                throw new IllegalArgumentException("invalid partition method " + pm);
            }
        }

        switch (spec.getMethod()) {
        case PARTITION_RATINGS:
            logger.debug("will partition ratings");
            cf.setMethod(CrossfoldMethods.partitionEntities());
            break;
        case PARTITION_USERS:
            logger.debug("will partition users");
            cf.setMethod(CrossfoldMethods.partitionUsers(order, part));
            break;
        case SAMPLE_USERS:
            logger.debug("will sample users");
            cf.setMethod(CrossfoldMethods.sampleUsers(order, part, spec.getSampleSize()));
            break;
        }

        // TODO Support custom class loader
        cf.setSource(SpecUtils.buildObject(DataSource.class, spec.getSource()));

        return cf;
    }

    /**
     * Get the entity type that this crossfolder will crossfold.
     * @return The entity type to crossfold.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Set the entity type that this crossfolder will crossfold.
     * @param entityType The entity type to crossfold.
     */
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
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
    public void execute() {
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
            StaticFileDAOProvider data = ((TextDataSource) source).getDataProvider();

            logger.info("ensuring output directory {} exists", outputDir);
            Files.createDirectories(outputDir);
            logger.info("making sure item list is available");
            JsonNode itemData = writeItemFile(data);
            logger.info("writing train-test split files");
            createTTFiles(data);
        } catch (IOException ex) {
            // TODO Use application-specific exception
            throw new RuntimeException("Error writing data sets", ex);
        }
    }

    List<Path> getTrainingFiles() {
        return getFileList("part%02d.train." + outputFormat.getExtension());
    }

    List<Path> getTrainingManifestFiles() {
        return getFileList("part%02d.train.yaml");
    }

    List<Path> getTestFiles() {
        return getFileList("part%02d.test." + outputFormat.getExtension());
    }

    List<Path> getTestManifestFiles() {
        return getFileList("part%02d.test.yaml");
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
     * Write the items to a file.
     * @param data The input data.
     * @return The JSON data to include in the manifest to describe the item file.
     * @throws IOException if there's a problem writing the file.
     */
    @Nullable
    private JsonNode writeItemFile(StaticFileDAOProvider data) throws IOException {
        List<EntitySource> itemSources = data.getSourcesForType(CommonTypes.ITEM);
        if (itemSources.isEmpty()) {
            logger.info("writing item IDs to {}", ITEM_FILE_NAME);
            Path itemFile = outputDir.resolve(ITEM_FILE_NAME);
            DataAccessObject dao = data.get();
            LongSet items = dao.getEntityIds(CommonTypes.ITEM);
            try (BufferedWriter writer = Files.newBufferedWriter(itemFile, Charsets.UTF_8)) {
                for (Long item: items) { // escape analysis should elide allocations
                    writer.append(item.toString())
                          .append(System.lineSeparator());
                }
            }

            // make the node describing this
            JsonNodeFactory fac = JsonNodeFactory.instance;
            ObjectNode node = fac.objectNode();
            node.set("type", fac.textNode("textfile"));
            node.set("format", fac.textNode("tsv"));
            node.set("file", fac.textNode(ITEM_FILE_NAME));
            return node;
        } else {
            logger.info("input data specifies an item source, reusing that");
            return null;
        }
    }

    /**
     * Write train-test split files.
     *
     * @throws IOException if there is an error writing the files.
     * @param data The input data.
     */
    private void createTTFiles(StaticFileDAOProvider data) throws IOException {
        if (entityType != CommonTypes.RATING) {
            logger.warn("entity type is not 'rating', crossfolding may not work correctly");
            logger.warn("crossfolding non-rating data is a work in progress");
        }

        List<EntitySource> sources = data.getSourcesForType(entityType);
        logger.info("crossfolding {} data from {} sources", entityType, sources);

        for (EntitySource source: sources) {
            Set<EntityType> types = source.getTypes();
            if (types.size() > 1) {
                logger.warn("source {} has multiple entity types", source);
                logger.warn("the following types will be ignored: {}",
                            Sets.difference(types, ImmutableSet.of(entityType)));
            }
        }

        try (CrossfoldOutput out = new CrossfoldOutput(this, rng)) {
            logger.info("running crossfold method {}", method);
            method.crossfold(data.get(), out, entityType);
        }

        List<Path> specFiles = getSpecFiles();
        List<DataSet> dataSets = getDataSets();
        Path fullSpecFile = getOutputDir().resolve("all-partitions.json");
        List<Object> specs = new ArrayList<>(partitionCount);
        assert dataSets.size() == partitionCount;
        for (int i = 0; i < partitionCount; i++) {
            Path file = specFiles.get(i);
            DataSet ds = dataSets.get(i);
            DataSetSpec spec = ds.toSpec();
            specs.add(spec);
            SpecUtils.write(spec, file);
        }

        SpecUtils.write(specs, fullSpecFile);
    }

    /**
     * Get the train-test splits as data sets.
     * 
     * @return The data sets produced by this crossfolder.
     */
    public List<DataSet> getDataSets() {
        List<DataSet> dataSets = new ArrayList<>(partitionCount);
        List<Path> trainFiles = getTrainingFiles();
        List<Path> testFiles = getTestFiles();
        for (int i = 0; i < partitionCount; i++) {
            DataSetBuilder dsb = new DataSetBuilder(getName() + "." + i);

            dataSets.add(dsb.setTest(makeDataSource(testFiles.get(i)))
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
            // it is a CSV file, and the file name already has compression
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
