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
package org.lenskit.specs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * A specification that is entirely dynamic, containing an arbitrary JSON tree.
 */
public class DynamicSpec extends AbstractSpec {
    private JsonNode json;

    /**
     * Create a new null dynamic spec.
     */
    public DynamicSpec() {
        json = NullNode.getInstance();
    }

    /**
     * Create a new dynamic spec wrapper.
     * @param node The dynamic spec wrapper.
     */
    @JsonCreator
    public DynamicSpec(JsonNode node) {
        json = node;
    }

    /**
     * Get the JSON node in this spec.
     * @return The JSON node in this spec.
     */
    @JsonValue
    public JsonNode getJSON() {
        return json;
    }

    /**
     * Set the JSON node in this spec.
     * @param js The JSON node to wrap.
     */
    public void setJSON(JsonNode js) {
        json = js;
    }
}
