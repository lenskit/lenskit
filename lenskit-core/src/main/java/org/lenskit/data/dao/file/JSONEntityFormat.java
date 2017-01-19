/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.lenskit.data.entities.*;
import org.lenskit.util.reflect.InstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Entity format that decodes JSON objects.
 */
public class JSONEntityFormat implements EntityFormat {
    private static final Logger logger = LoggerFactory.getLogger(JSONEntityFormat.class);
    private EntityType entityType;
    private Class<? extends EntityBuilder> entityBuilder = BasicEntityBuilder.class;
    private InstanceFactory<EntityBuilder> builderFactory;

    /**
     * Set the entity type.
     * @param type The entity type.
     */
    public void setEntityType(EntityType type) {
        entityType = type;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public int getHeaderLines() {
        return 0;
    }

    @Override
    public LineEntityParser makeParser(List<String> header) {
        Preconditions.checkArgument(header.isEmpty(), "JSON does not have headers");
        return new JSONLP();
    }

    /**
     * Set the entity builder class.
     * @param builder The entity builder class.
     */
    public void setEntityBuilder(Class<? extends EntityBuilder> builder) {
        entityBuilder = builder;
        builderFactory = null;
    }

    /**
     * Get the entity builder class.
     * @return The entity builder class.
     */
    public Class<? extends EntityBuilder> getEntityBuilder() {
        return entityBuilder;
    }

    /**
     * Instantiate a new entity builder.
     * @return A new entity builder.
     */
    public EntityBuilder newEntityBuilder() {
        if (builderFactory == null) {
            builderFactory = InstanceFactory.fromConstructor(entityBuilder, entityType);
        }
        return builderFactory.newInstance();
    }

    @Override
    public ObjectNode toJSON() {
        return null;
    }

    private class JSONLP extends LineEntityParser {
        private final ObjectMapper mapper;
        int lineNo = 0;

        JSONLP() {
            mapper = new ObjectMapper();
        }

        @Override
        public Entity parse(String line) {
            lineNo += 1;

            JsonNode node = null;
            try {
                node = mapper.readTree(line);
            } catch (IOException e) {
                throw new RuntimeException("cannot parse line " + lineNo, e);
            }

            if (node.isObject()) {
                EntityBuilder eb = newEntityBuilder();
                JsonNode idNode = node.get("$id");
                if (idNode != null) {
                    eb.setId(node.get("$id").asLong());
                } else {
                    logger.debug("line " + lineNo + ": using -(row number) as id");
                    eb.setId(-lineNo);
                }
                Iterator<Map.Entry<String,JsonNode>> fields = node.fields();
                while (fields.hasNext()) {
                    Map.Entry<String,JsonNode> field = fields.next();
                    String name = field.getKey();
                    if (name.startsWith("$")) {
                        continue;
                    }
                    JsonNode fn = field.getValue();
                    if (fn.isIntegralNumber()) {
                        eb.setAttribute(TypedName.create(name, Long.class), fn.asLong());
                    } else if (fn.isFloatingPointNumber()) {
                        eb.setAttribute(TypedName.create(name, Double.class), fn.asDouble());
                    } else if (fn.isTextual()) {
                        eb.setAttribute(TypedName.create(name, String.class), fn.asText());
                    } else if (fn.isContainerNode()) {
                        // FIXME Be more flexible about resulting types
                        eb.setAttribute(TypedName.create(name, JsonNode.class), fn);
                    }
                }
                return eb.build();
            } else {
                throw new IllegalArgumentException("line " + lineNo + ": not an object");
            }
        }
    }
}
