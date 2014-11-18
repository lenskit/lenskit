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
package org.grouplens.lenskit.specs;

import com.typesafe.config.Config;

/**
 * Base interface for building objects from specifications.  Individual object configurator
 * interfaces will extend this interface, and their implementations will be resolved using
 * {@link java.util.ServiceLoader}.
 */
public interface SpecHandler<T> {
    /**
     * Ask if this configurator can handle the specified type.
     * @param type The name of the type.
     * @return {@code true} if this configurator can handle a configuration with the specified
     * {@code type} (as its {@code type} key).
     */
    boolean handlesType(String type);

    /**
     * Build an object from a configuration.
     * @param context The configuration context.
     * @param cfg The configuration.
     * @return The configured object.
     * @throws java.lang.IllegalArgumentException if {@code cfg} is not a configuration of this type
     * of object.
     * @throws SpecificationException if there is an error in the configuration.
     */
    T buildFromSpec(SpecificationContext context, Config cfg) throws SpecificationException;
}
