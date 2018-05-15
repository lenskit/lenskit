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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.lenskit.data.entities.*;
import org.lenskit.util.TypeUtils;
import org.lenskit.util.reflect.InstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Entity format that decodes JSON objects.
 */
public class JSONEntityFormat implements EntityFormat {
    private static final Logger logger = LoggerFactory.getLogger(JSONEntityFormat.class);
    private EntityType entityType;
    private Class<? extends EntityBuilder> entityBuilder = BasicEntityBuilder.class;
    private InstanceFactory<EntityBuilder> builderFactory;
    private Map<String,TypedName<?>> attributes = new LinkedHashMap<>();

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
    @Nonnull
    @Override
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

    /**
     * Get the attributes expected.
     * @return The expected attributes.
     */
    public Map<String,TypedName<?>> getDefinedAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public AttributeSet getAttributes() {
        if (attributes.isEmpty()) {
            return null;
        }

        List<TypedName<?>> attrs = new ArrayList<>(attributes.values());
        if (!attrs.contains(CommonAttributes.ENTITY_ID)) {
            attrs.add(0, CommonAttributes.ENTITY_ID);
        }
        return AttributeSet.create(attrs);
    }

    /**
     * Add an attribute to the list of expected attributes.  It will be parsed from the JSON object field of the same
     * name.
     * @param attr The attribute to add.
     */
    public void addAttribute(TypedName<?> attr) {
        addAttribute(attr.getName(), attr);
    }

    /**
     * Add an attribute to the list of expected attributes, with a JSON field name.
     * @param name The name of the field as it will appear in JSON.
     * @param attr The attribute to add.
     */
    public void addAttribute(String name, TypedName<?> attr) {
        attributes.put(name, attr);
    }

    @Override
    public ObjectNode toJSON() {
        JsonNodeFactory nf = JsonNodeFactory.instance;

        ObjectNode json = nf.objectNode();
        json.put("format", "json");
        json.put("entity_type", entityType.getName());

        if (!attributes.isEmpty()) {
            ObjectNode attrNode = json.putObject("attributes");
            for (Map.Entry<String,TypedName<?>> attr: attributes.entrySet()) {
                ObjectNode an = attrNode.putObject(attr.getKey());
                an.put("name", attr.getValue().getName());
                an.put("type", TypeUtils.makeTypeName(attr.getValue().getType()));
            }
        }

        return json;
    }

    public static JSONEntityFormat fromJSON(String name, ClassLoader loader, JsonNode json) {
        JSONEntityFormat format = new JSONEntityFormat();

        String eTypeName = json.path("entity_type").asText().toLowerCase();
        EntityType etype = EntityType.forName(eTypeName);
        logger.debug("{}: reading entities of type {}", name, etype);
        EntityDefaults entityDefaults = EntityDefaults.lookup(etype);
        format.setEntityType(etype);
        format.setEntityBuilder(entityDefaults != null ? entityDefaults.getDefaultBuilder() : BasicEntityBuilder.class);

        JsonNode attrNode = json.path("attributes");
        if (attrNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fieldIter = attrNode.fields();
            while (fieldIter.hasNext()) {
                Map.Entry<String, JsonNode> fieldSpec = fieldIter.next();
                String fname = fieldSpec.getKey();
                JsonNode fnode = fieldSpec.getValue();
                if (fnode.isTextual()) {
                    format.addAttribute(TypedName.create(fname, fnode.asText()));
                } else if (fnode.isObject()) {
                    format.addAttribute(fname, TypedName.create(fnode.get("name").asText(),
                                                                fnode.get("type").asText()));
                } else {
                    throw new IllegalArgumentException("unexpected structure for field " + fname);
                }
            }
        } else if (!attrNode.isMissingNode() && !attrNode.isNull()) {
            throw new IllegalArgumentException("unexpected structure for fields configuration");
        }

        Class<? extends EntityBuilder> eb = TextEntitySource.parseEntityBuilder(loader, json);
        if (eb != null) {
            format.setEntityBuilder(eb);
        }

        return format;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", entityType)
                .append("builder", entityBuilder)
                .toString();
    }

    private class JSONLP extends LineEntityParser {
        private final ObjectMapper mapper;
        int lineNo = 0;
        boolean warned = false;

        JSONLP() {
            mapper = new ObjectMapper();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Entity parse(String line) {
            lineNo += 1;

            JsonNode node = null;
            try {
                node = mapper.readTree(line);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot parse line " + lineNo, e);
            }

            if (node.isObject()) {
                EntityBuilder eb = newEntityBuilder();
                JsonNode idNode = node.get("$id");
                if (idNode != null) {
                    eb.setId(node.get("$id").asLong());
                } else {
                    if (!warned) {
                        logger.debug("line {}: using -(row number) as id", lineNo);
                        warned = true;
                    }
                    eb.setId(-lineNo);
                }
                Iterator<Map.Entry<String,JsonNode>> fields = node.fields();
                while (fields.hasNext()) {
                    Map.Entry<String,JsonNode> field = fields.next();
                    String name = field.getKey();
                    if (name.startsWith("$")) {
                        continue;
                    }
                    TypedName attr = attributes.get(name);
                    if (attr == null && !attributes.isEmpty()) {
                        // unknown attribute, skip it
                        continue;
                    }

                    JsonNode fn = field.getValue();
                    if (fn.isNull()) {
                        continue; // just skip nulls
                    }
                    if (attr != null) {
                        eb.setAttribute(attr, mapper.convertValue(fn, attr.getJacksonType()));
                    } else {
                        if (fn.isIntegralNumber()) {
                            eb.setAttribute(TypedName.create(name, Long.class), fn.asLong());
                        } else if (fn.isFloatingPointNumber()) {
                            eb.setAttribute(TypedName.create(name, Double.class), fn.asDouble());
                        } else if (fn.isTextual()) {
                            eb.setAttribute(TypedName.create(name, String.class), fn.asText());
                        } else {
                            eb.setAttribute(TypedName.create(name, JsonNode.class), fn);
                        }
                    }
                }
                return eb.build();
            } else {
                throw new IllegalArgumentException("line " + lineNo + ": not an object");
            }
        }
    }
}
