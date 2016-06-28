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
import org.lenskit.data.dao.DataAccessException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.*;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.*;

/**
 * Layout and builder for DAOs backed by static files.  This is used to read CSV files
 * and the like; it controls a composite DAO that reads from files, caches them in
 * memory, and can compute some derived entities from others (e.g. extracting items
 * from the item IDs in a rating data set).
 */
public class StaticFileDAOProvider implements Provider<DataAccessObject> {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileDAOProvider.class);
    private List<EntitySource> sources;
    private ListMultimap<EntityType, TypedName<?>> indexedAttributes;
    private transient volatile SoftReference<DataAccessObject> cachedDao;

    /**
     * Construct a new data layout object.
     */
    public StaticFileDAOProvider() {
        sources = new ArrayList<>();
        indexedAttributes = ArrayListMultimap.create();
    }

    /**
     * Add a collection data source.
     * @param data The entities to add.
     */
    public void addSource(Collection<? extends Entity> data) {
        sources.add(new CollectionEntitySource(data));
    }

    private void addSource(EntitySource source) {
        sources.add(source);
    }

    /**
     * Index entities by an attribute.
     * @param type The entity type to index.
     * @param attr The attribute to index.
     */
    public void addIndex(EntityType type, TypedName<?> attr) {
        indexedAttributes.put(type, attr);
    }

    /**
     * Get the data access object. This method is thread-safe.
     * @return The access object.
     */
    @Override
    public DataAccessObject get() {
        SoftReference<DataAccessObject> cache = cachedDao;
        DataAccessObject dao = cache != null ? cache.get() : null;
        if (dao == null) {
            synchronized (this) {
                // did someone else make a DAO?
                cache = cachedDao;
                dao = cache != null ? cache.get() : null;
                if (dao == null) {
                    try {
                        dao = makeDAO();
                        cachedDao = new SoftReference<>(dao);
                    } catch (IOException e) {
                        throw new DataAccessException("cannot load data", e);
                    }
                }
            }
        }

        return dao;
    }

    private DataAccessObject makeDAO() throws IOException {
        Set<EntityType> types = new HashSet<>();

        EntityCollectionDAOBuilder builder = new EntityCollectionDAOBuilder();
        for (Map.Entry<EntityType,TypedName<?>> iae: indexedAttributes.entries()) {
            builder.addIndex(iae.getKey(), iae.getValue());
        }
        for (EntitySource source: sources) {
            try (ObjectStream<Entity> data = source.openStream()) {
                for (Entity e: data) {
                    builder.addEntity(e);
                    types.add(e.getType());
                }
            }
        }

        for (EntityType type: types) {
            EntityDefaults defaults = EntityDefaults.lookup(type);
            if (defaults == null) {
                continue;
            }
            for (EntityDerivation deriv: defaults.getDefaultDerivations()) {
                EntityType derived = deriv.getType();
                if (types.contains(derived)) {
                    continue;
                }
                TypedName<Long> column = deriv.getAttribute();
                logger.info("deriving entity type {} from {} (column {})",
                            derived, deriv.getSourceTypes(), column);
                builder.deriveEntities(derived, type, column);
            }
        }

        return builder.build();
    }

    /**
     * Parse a JSON description of a data set.
     *
     * @param object The JSON object.
     * @return A DAO provider configured from the JSON data.
     */
    public static StaticFileDAOProvider fromJSON(JsonNode object, URI base) {
        StaticFileDAOProvider layout = new StaticFileDAOProvider();

        if (object.isArray()) {
            for (JsonNode source: object) {
                configureDataSource(layout, null, source, base);
            }
        } else if (object.isObject()) {
            if (object.has("file") || object.has("type")) {
                // the whole object describes one data source
                configureDataSource(layout, null, object, base);
            } else {
                // the object describes multiple data sources
                Iterator<Map.Entry<String, JsonNode>> iter = object.fields();
                while (iter.hasNext()) {
                    Map.Entry<String, JsonNode> entry = iter.next();
                    configureDataSource(layout, entry.getKey(), entry.getValue(), base);
                }
            }
        } else {
            throw new IllegalArgumentException("manifest must be array or object");
        }

        return layout;
    }

    private static void configureDataSource(StaticFileDAOProvider layout, String name, JsonNode object, URI base) {
        if (name == null) {
            name = object.path("name").asText("<unnamed>");
        }

        EntitySource source;
        String type = object.path("type").asText("textfile").toLowerCase();
        switch (type) {
        case "textfile":
            source = TextEntitySource.fromJSON(name, object, base);
            break;
        default:
            throw new IllegalArgumentException("invalid data source type: " + type);
        }

        layout.addSource(source);
    }
}
