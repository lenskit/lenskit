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
package org.lenskit.gradle.delegates

import org.gradle.api.Project
import org.lenskit.gradle.traits.DataSources
import org.lenskit.specs.eval.DataSetSpec

class DataSetSpecDelegate extends SpecDelegate implements DataSources {
    DataSetSpec dss

    public DataSetSpecDelegate(Project prj, DataSetSpec spec) {
        super(prj, spec);
        dss = spec;
    }

    /**
     * Get the map of attributes in the spec.
     * @return The spec's attributes
     */
    public Map<String,Object> getAttributes() {
        return dss.attributes
    }

    /**
     * Add an attribute.
     * @param name The attribute name.
     * @param val The attribute value.
     */
    public void attribute(String name, Object val) {
        dss.attributes[name] = val
    }

    /**
     * Add one or more attributes.
     * @param attrs The attributes to add.
     */
    public void attributes(Map<String,Object> attrs) {
        dss.attributes.putAll(attrs);
    }
}
