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
package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.lenskit.LenskitConfiguration;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * A train-test data set.
 */
public class DataSet {
    @Nonnull
    private final String name;
    @Nonnull
    private final StaticDataSource trainData;
    @Nullable
    private final StaticDataSource runtimeData;
    @Nonnull
    private final StaticDataSource testData;
    @Nonnull
    private final Provider<LongSet> testUserProvider;

    private volatile transient LongSortedSet allItems;
    @Nonnull
    private final UUID group;
    private final Map<String, Object> attributes;
    @Nonnull
    private final List<EntityType> entityTypes;

    /**
     * Create a new data set.
     * @param name The name.
     * @param train The training source.
     * @param test The test data source.
     * @param grp The data set isolation group.
     * @param attrs The data set attributes.
     */
    public DataSet(@Nonnull String name,
                   @Nonnull StaticDataSource train,
                   @Nullable StaticDataSource rt,
                   @Nonnull StaticDataSource test,
                   @Nonnull UUID grp,
                   Map<String, Object> attrs,
                   @Nonnull List<EntityType> entityTypes) {
        Preconditions.checkNotNull(train, "no training data");
        Preconditions.checkNotNull(test, "no test data");
        this.name = name;
        trainData = train;
        runtimeData = rt;
        testData = test;
        group = grp;
        if (attrs == null) {
            attributes = Collections.emptyMap();
        } else {
            attributes = ImmutableMap.copyOf(attrs);
        }
        this.entityTypes = ImmutableList.copyOf(entityTypes);

        testUserProvider = new Provider<LongSet>() {
            @Override
            public LongSet get() {
                return testData.get().getEntityIds(CommonTypes.USER);
            }
        };
    }



    /**
     * Get the data set name.
     *
     * @return A name for the data set. Used in the output file.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the data set attributes (used for identification in output).
     *
     * @return A key &rarr; value map of the attributes used to identify this data
     *         set. For example, a crossfold data set may include the source
     *         name and fold number.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the isolation group ID for this data set.  Data sets in the same group will be allowed to
     * run in parallel.  This is used to implement data set isolation.
     *
     * @return The group ID for this data set.
     */
    public UUID getIsolationGroup() {
        return group;
    }

    /**
     * Get the training data.
     *
     * @return A data source containing the test data.
     */
    @Nonnull
    public StaticDataSource getTestData() {
        return testData;
    }

    /**
     * Get the training data.
     *
     * @return A data source containing the training data.
     */
    @Nonnull
    public StaticDataSource getTrainingData() {
        return trainData;
    }

    /**
     * Get the runtime data set. This is the data that should be available when the recommender is run,
     * but not when its model is trained.
     *
     * @return The runtime data set.
     */
    @Nullable
    public StaticDataSource getRuntimeData() {
        return runtimeData;
    }

    public LongSet getAllItems() {
        if (allItems == null) {
            synchronized (this) {
                if (allItems == null) {
                    allItems = LongUtils.packedSet(trainData.get().getEntityIds(CommonTypes.ITEM));
                }
            }
        }

        return allItems;
    }

    /**
     * Get the entity types registered with this builder so far.
     * @return The entity types registered so far.
     */
    @Nonnull
    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    /**
     * Get extra LensKit configuration required by this data set.
     *
     * @return A LensKit configuration with additional configuration data for this data set.
     */
    public LenskitConfiguration getExtraConfiguration() {
        LenskitConfiguration config = new LenskitConfiguration();
        PreferenceDomain pd = trainData.getPreferenceDomain();
        if (pd != null) {
            config.bind(PreferenceDomain.class).to(pd);
        }
        config.bind(TestUsers.class, LongSet.class)
              .toProvider(testUserProvider);
        return config;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataSet(")
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

    /**
     * Create a new generic train-test data set builder.
     * @return The new builder.
     */
    public static DataSetBuilder newBuilder() {
        return new DataSetBuilder();
    }

    /**
     * Create a new generic train-test data set builder.
     * @param name The data set name.
     * @return The new builder.
     */
    public static DataSetBuilder newBuilder(String name) {
        return new DataSetBuilder(name);
    }

    /**
     * Create a new builder initialized with this data set's values.
     * @return A new builder initialized to make a copy of this data set definition.
     */
    public DataSetBuilder copyBuilder() {
        return copyBuilder(this);
    }

    /**
     * Create a new builder initialized with this data set's values.
     * @return A new builder initialized to make a copy of this data set definition.
     */
    public static DataSetBuilder copyBuilder(DataSet data) {
        DataSetBuilder builder = newBuilder(data.getName());
        builder.setTest(data.getTestData())
               .setTrain(data.getTrainingData())
               .setIsolationGroup(data.getIsolationGroup());
        for (Map.Entry<String,Object> attr: data.getAttributes().entrySet()) {
            builder.setAttribute(attr.getKey(), attr.getValue());
        }
        return builder;
    }

    /**
     * Load one or more data sets from JSON data.
     */
    public static List<DataSet> fromJSON(JsonNode json, URI base) throws IOException {
        if (!json.has("name")) {
            throw new IllegalArgumentException("no data set name");
        }
        String name = json.get("name").asText();
        List<DataSet> sets = new ArrayList<>();

        // some magic for internal train-test gradle munging
        if (json.has("base_uri")) {
            base = base.resolve(json.get("base_uri").asText());
        }

        if (json.has("datasets")) {
            JsonNode list = json.get("datasets");
            int n = 0;
            for (JsonNode node: list) {
                n++;
                sets.add(loadDataSet(node, base, name, n));
            }
        } else {
            sets.add(loadDataSet(json, base, name, -1));
        }

        ImmutableList.Builder<DataSet> finalSets = ImmutableList.builder();
        boolean isolate = json.path("isolate").asBoolean(false);
        if (isolate) {
            for (DataSet set: sets) {
                finalSets.add(set.copyBuilder()
                                 .setIsolationGroup(UUID.randomUUID())
                                 .build());
            }
        } else {
            finalSets.addAll(sets);
        }

        return finalSets.build();
    }

    /**
     * Load a single data set.
     * @param json The JSON node.
     * @param base The base URI.
     * @param name The name
     * @param part The partition number
     * @return The data source.
     */
    private static DataSet loadDataSet(JsonNode json, URI base, String name, int part) throws IOException {
        Preconditions.checkArgument(json.has("train"), "%s: no train data specified", name);
        Preconditions.checkArgument(json.has("test"), "%s: no test data specified", name);

        List<EntityType> entityList = new ArrayList<>();

        JsonNode etNode = json.path("entity_types");
        if (etNode.isArray()) {
            for (JsonNode node : etNode) {
                entityList.add(EntityType.forName(node.asText()));
            }
        } else if (etNode.isTextual()) {
            entityList.add(EntityType.forName(etNode.asText()));
        } else if (etNode.isMissingNode() || etNode.isNull()) {
            entityList.add(CommonTypes.RATING);
        } else {
            throw new IllegalArgumentException("unexpected format for entity_types");
        }

        DataSetBuilder dsb = newBuilder(name);

        dsb.setEntityTypes(entityList);
        if (part >= 0) {
            dsb.setName(String.format("%s[%d]", name, part))
               .setAttribute("DataSet", name)
               .setAttribute("Partition", part);
        }
        String nbase = part >= 0 ? String.format("%s[%d]", name, part) : name;
        dsb.setTrain(loadDataSource(json.get("train"), base, nbase + ".train"));
        dsb.setTest(loadDataSource(json.get("test"), base, nbase + ".test"));
        if (json.hasNonNull("runtime")) {
            dsb.setRuntime(loadDataSource(json.get("runtime"), base, nbase + ".runtime"));
        }
        if (json.has("group")) {
            dsb.setIsolationGroup(UUID.fromString(json.get("group").asText()));
        }
        return dsb.build();
    }

    /**
     * Load a single data source.
     * @param json The JSON node.
     * @param base The base URI.
     * @return The data source.
     */
    private static StaticDataSource loadDataSource(JsonNode json, URI base, String name) throws IOException {
        StaticDataSource source;

        if (json.isTextual()) {
            URI uri = base.resolve(json.asText());
            source = StaticDataSource.load(uri, name);
        } else {
            source = StaticDataSource.fromJSON(name, json, base);
        }

        return source;
    }

    /**
     * Load one or more data sets from a YAML manifest file.
     * @param file The path to the YAML manifest file.
     * @return The list of data sets.
     */
    public static List<DataSet> load(Path file) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode node = mapper.readTree(file.toFile());
        return fromJSON(node, file.toAbsolutePath().toUri());
    }

    /**
     * Load one or more data sets from a YAML manifest file.
     * @param url The URL of a the YAML manifest file.
     * @return The list of data sets.
     */
    public static List<DataSet> load(URL url) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode node = mapper.readTree(url);
        try {
            return fromJSON(node, url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL is not a valid URI", e);
        }
    }
}
