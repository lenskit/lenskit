/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.util.spi;

import javax.annotation.Nullable;

/**
 * Base interface for configurable modules. Interfaces extending this interface
 * allow implementations to provide a short alias for easier reference in
 * configuration files.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface Selectable {
    /**
     * Get a short configuration name for this module.
     * 
     * @return A short name that can be used to refer to this implementation in
     *         configuration files.  If <tt>null</tt>, the class's name must
     *         be used.
     */
    @Nullable
    String getConfigName();
}
