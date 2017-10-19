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
package org.lenskit.eval.crossfold;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.dao.DataAccessException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.EntitySource;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.dao.file.TextEntitySource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.output.OutputFormat;
import org.lenskit.data.output.RatingWriter;
import org.lenskit.data.output.RatingWriters;
import org.lenskit.eval.traintest.DataSet;
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
 * Partitions a data set for cross-validation.
 *
 * The resulting data is placed in an output directory with the following files:
 *
 * - `datasets.yaml` - a manifest file listing all the data sets
 * - `partNN.train.csv` - a CSV file containing the train data for part *NN*
 * - `partNN.train.yaml` - a YAML manifest for the training data for part *NN*
 * - `partNN.test.csv` - a CSV file containing the test data for part *NN*
 * - `partNN.test.yaml` - a YAML manifest for the test data for part *NN*
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Crossfolder {
    public static final String ITEM_FILE_NAME = "items.txt";

    private static final Logger logger = LoggerFactory.getLogger(Crossfolder.class);

    private Random rng;
    private String name;
    private StaticDataSource source;
    private EntityType entityType = CommonTypes.RATING;
    private int partitionCount = 5;
    private Path outputDir;
    private OutputFormat outputFormat = OutputFormat.CSV;
    private CrossfoldMethod method = CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10));
    private boolean writeTimestamps = true;
    private boolean executed = false;

    public Crossfolder() {
        this(null);
    }

    public Crossfolder(String n) {
        name = n;
        rng = new Random();
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
     * Set the data source.
     * @param src
     * @return The crossfolder (for chaining)
     */
    public Crossfolder setSource(StaticDataSource src) {
        source = src;
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
    public StaticDataSource getSource() {
        return source;
    }

    /**
     * Run the crossfold command. Write the partition files to the disk by reading in the source file.
     */
    public void execute() throws IOException {
        logger.info("ensuring output directory {} exists", outputDir);
        Files.createDirectories(outputDir);
        logger.info("making sure item list is available");
        JsonNode itemDataInfo = writeItemFile(source);
        logger.info("writing train-test split files");
        createTTFiles(source);
        logger.info("writing manifests and specs");
        Map<String,Object> metadata = new HashMap<>();
        for (EntitySource src: source.getSourcesForType(entityType)) {
            metadata.putAll(src.getMetadata());
        }
        writeManifests(source, metadata, itemDataInfo);
        executed = true;
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
    private JsonNode writeItemFile(StaticDataSource data) throws IOException {
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

            logger.info("wrote {} item IDs", items.size());

            // make the node describing this
            JsonNodeFactory fac = JsonNodeFactory.instance;
            ObjectNode node = fac.objectNode();
            node.set("name", fac.textNode(getName() + ".items"));
            node.set("type", fac.textNode("textfile"));
            node.set("format", fac.textNode("tsv"));
            node.set("file", fac.textNode(ITEM_FILE_NAME));
            node.set("entity_type", fac.textNode(CommonTypes.ITEM.getName()));
            ArrayNode cols = fac.arrayNode();
            cols.add(CommonAttributes.ENTITY_ID.getName());
            node.set("columns", cols);
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
    private void createTTFiles(StaticDataSource data) throws IOException {
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
    }

    private void writeManifests(StaticDataSource data, Map<String,Object> meta, JsonNode itemData) throws IOException {
        logger.debug("writing manifests");
        YAMLFactory ioFactory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(ioFactory);

        JsonNodeFactory nf = JsonNodeFactory.instance;

        List<Path> trainFiles = getTrainingFiles();
        List<Path> trainManifestFiles = getTrainingManifestFiles();
        List<Path> testFiles = getTestFiles();
        List<Path> testManifestFiles = getTestManifestFiles();
        Path dataSetFile = outputDir.resolve("datasets.yaml");

        ObjectNode dsNode = nf.objectNode();
        dsNode.set("name", nf.textNode(name));
        ArrayNode dsList = nf.arrayNode();

        for (int i = 0; i < partitionCount; i++) {
            ObjectNode dsListEntry = nf.objectNode();
            dsListEntry.set("train", nf.textNode(outputDir.relativize(trainManifestFiles.get(i)).toString()));
            dsListEntry.set("test", nf.textNode(outputDir.relativize(testManifestFiles.get(i)).toString()));
            dsList.add(dsListEntry);

            // TODO Support various columns in crossfold output
            logger.debug("writing train manifest {}", i);
            Path trainFile = trainManifestFiles.get(i);
            ArrayNode trainList = nf.arrayNode();
            ObjectNode train = nf.objectNode();
            train.set("name", nf.textNode(String.format("%s.%d.train", getName(), i)));
            train.set("type", nf.textNode("textfile"));
            train.set("file", nf.textNode(outputDir.relativize(trainFiles.get(i)).toString()));
            train.set("format", nf.textNode("csv"));
            train.set("entity_type", nf.textNode(entityType.getName()));
            train.set("metadata", mapper.valueToTree(meta));
            trainList.add(train);

            // write the item output
            if (itemData != null) {
                trainList.add(itemData);
            }

            // write the other data files
            for (EntitySource source: data.getSources()) {
                if (source.getTypes().contains(entityType)) {
                    continue; // this one was crossfolded
                }
                if (source instanceof TextEntitySource) {
                    trainList.add(((TextEntitySource) source).toJSON(trainFile.toUri()));
                } else {
                    logger.warn("ignoring non-file data source {}", source);
                }
            }
            mapper.writeValue(trainFile.toFile(), trainList);

            logger.debug("writing test manifest {}", i);
            ObjectNode test = nf.objectNode();
            test.set("name", nf.textNode(String.format("%s.%d.test", getName(), i)));
            test.set("type", nf.textNode("textfile"));
            test.set("file", nf.textNode(outputDir.relativize(testFiles.get(i)).toString()));
            test.set("format", nf.textNode("csv"));
            test.set("entity_type", nf.textNode(entityType.getName()));
            test.set("metadata", mapper.valueToTree(meta));
            mapper.writeValue(testManifestFiles.get(i).toFile(), test);
        }

        dsNode.set("datasets", dsList);

        mapper.writeValue(dataSetFile.toFile(), dsNode);
    }

    /**
     * Get the train-test splits as data sets.
     * 
     * @return The data sets produced by this crossfolder.
     */
    public List<DataSet> getDataSets() {
        Preconditions.checkState(executed, "crossfolder has not been executed");

        Path dataSetFile = outputDir.resolve("datasets.yaml");
        try {
            return DataSet.load(dataSetFile);
        } catch (IOException e) {
            throw new DataAccessException("cannot load data sets", e);
        }
    }

    RatingWriter openWriter(Path file) throws IOException {
        return RatingWriters.csv(file.toFile(), writeTimestamps);
    }

    @Override
    public String toString() {
        return String.format("{CXManager %s}", source);
    }
}
