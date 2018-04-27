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
