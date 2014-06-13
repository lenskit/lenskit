/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graph node.
 */
class GVNode {
    private final String id;
    private final Map<String,Object> attributes;
    private final String target;

    /**
     * Construct a new graph node.
     * @param id The node ID.
     * @param attrs The node attributes.
     * @param tgt The node target (ID, possibly with port). Used for drawing edges to this
     *            node.
     */
    public GVNode(String id, Map<String, Object> attrs, String tgt) {
        this.id = id;
        attributes = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(attrs));
        target = tgt;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getTarget() {
        return target;
    }
}
