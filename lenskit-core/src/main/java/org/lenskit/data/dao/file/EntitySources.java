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
import org.lenskit.data.entities.EntityDerivation;

import java.net.URI;

/**
 * Utility classes for loading and working with entity sources.
 */
public class EntitySources {
    /**
     * Configure an entity source from JSON data.
     * @param object The JSON data.
     * @param base The base URI.
     * @return The newly-configured entity source.
     */
    public static EntitySource fromJSON(JsonNode object, URI base) {
        return fromJSON(null, object, base);
    }

    static EntitySource fromJSON(String name, JsonNode object, URI base) {
        return fromJSON(name, object, base, null);
    }

    static EntitySource fromJSON(String name, JsonNode object, URI base, ParseHandler handler) {
        if (name == null) {
            name = object.path("name").asText("<unnamed>");
        }

        EntitySource source;
        String type = object.path("type").asText("textfile").toLowerCase();
        switch (type) {
            case "derived":
                if (handler == null) {
                    throw new IllegalArgumentException("cannot parse derivation without handler");
                }
                handler.handleEntityDerivation(EntityDerivation.fromJSON(object));
                return null;
            case "textfile":
                source = TextEntitySource.fromJSON(name, object, base);
                break;
            default:
                throw new IllegalArgumentException("invalid data source type: " + type);
        }

        if (handler != null) {
            handler.handleEntitySource(source);
        }
        return source;
    }

    /**
     * Handler for parsing entity sources.
     */
    interface ParseHandler {
        /**
         * An entity source has been parsed.
         * @param source The source.
         */
        void handleEntitySource(EntitySource source);

        /**
         * An entity derivation has been parsed.
         * @param deriv The derivation.
         */
        void handleEntityDerivation(EntityDerivation deriv);
    }
}
