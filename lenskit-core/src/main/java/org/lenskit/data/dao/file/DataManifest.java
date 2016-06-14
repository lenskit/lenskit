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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.lenskit.data.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Description of the layout for a common entity DAO.  Layouts consist of entity sources,
 * indexes, and derived entities.
 *
 * These DAOs are only suitable for static data, such as CSV files and the like.
 */
public class DataManifest {
    private static final Logger logger = LoggerFactory.getLogger(DataManifest.class);
    private ListMultimap<EntityType, ?> sources;
    private ListMultimap<EntityType, Attribute<?>> indexedAttributes;

    /**
     * Construct a new data layout object.
     */
    public DataManifest() {
        sources = ArrayListMultimap.create();
        indexedAttributes = ArrayListMultimap.create();
    }

    /**
     * Add a collection data source.
     * @param data The entities to add.
     */
    public void addSource(Collection<? extends Entity> data) {
        // TODO Implement
    }

    private void addSource(TextEntityReader source) {
        // FIXME Add the source
    }

    /**
     * Index entities by an attribute.
     * @param type The entity type to index.
     * @param attr The attribute to index.
     */
    public void addIndex(EntityType type, Attribute<?> attr) {
        // TODO Implement
    }

    /**
     * Add an entity to be derived from a column in another entity.  These entities will have
     * no additional data, but their IDs will be available.
     *
     * @param type The entity type to derive.
     * @param src The entity type from which to source entities.
     * @param attr The column to use.
     */
    public void addDerivedEntity(EntityType type, EntityType src, Attribute<Long> attr) {
        // TODO implement
    }

    /**
     * Parse a JSON description of a data set.
     *
     * @param object The JSON object.
     * @return The data layout.
     */
    public static DataManifest fromJSON(JsonNode object, Path dir) {
        DataManifest layout = new DataManifest();

        if (object.isArray()) {
            for (JsonNode source: object) {
                configureDataSource(layout, null, source, dir);
            }
        } else if (object.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iter = object.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                configureDataSource(layout, entry.getKey(), entry.getValue(), dir);
            }
        } else {
            throw new IllegalArgumentException("manifest must be array or object");
        }

        return layout;
    }

    private static void configureDataSource(DataManifest layout, String name, JsonNode object, Path dir) {
        if (name == null) {
            name = object.path("name").asText("<unnamed>");
        }

        String type = object.path("type").asText("textfile").toLowerCase();
        if (!type.equals("textfile")) {
            // FIXME Delegate file formats
            throw new IllegalArgumentException("unsupported data type " + type);
        }

        TextEntityReader source = TextEntityReader.fromJSON(name, object, dir);

        layout.addSource(source);
    }
}
