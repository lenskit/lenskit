/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.util.dtree;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract interface for data nodes. They are like XML nodes, and are used to
 * represent evaluator configurations. Using these rather than directly using
 * XML DOM nodes allows greater flexibility in configuration, such as embedding
 * configuration directly in Maven POMs.
 * 
 * <p>This interface is heavily inspired by the Plexus configuration interface.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface DataNode {
    /**
     * Get the name of this node.  In XML, this would be the element name.
     * @return The node's name.
     */
    @Nonnull String getName();
    
    /**
     * Get the value of this node.  In XML, this is its text content.
     * @return The node's text value.
     */
    @Nonnull String getValue();
    
    /**
     * Get the raw (untrimmed) value of this node.
     */
    @Nonnull String getRawValue();
    
    /**
     * Get the value for an attribute of this node.
     * 
     * @param name The attribute name.
     * @return The attribute value, or <tt>null</tt> if no such attribute is
     *         present.
     */
    @Nullable String getAttribute(String name);
    
    /**
     * Get the value for an attribute of this node with a default.
     * @param name The attribute name.
     * @param dft The default value.
     * @return The attribute value, or <var>dft</var> if no such attribute is
     *         present.
     */
    String getAttribute(String name, String dft);
    
    /**
     * Get the children of this node.
     * @return The node's children.
     */
    @Nonnull List<DataNode> getChildren();
    
    /**
     * Get all children having a particular name.
     * @param name The name to look for.
     * @return All children named <var>name</var>.
     */
    @Nonnull List<DataNode> getChildren(String name);
}
