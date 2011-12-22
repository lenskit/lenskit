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
package org.grouplens.lenskit.eval;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.grouplens.lenskit.dtree.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for parsing configurations.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    
    public static DirectoryScanner configureScanner(DataNode node) {
        File base = new File(node.getAttribute("dir", "."));
        logger.debug("Creating scanner in `{}'", base);
        
        String[] includes = getPatterns(node.getChildren("include"));
        logger.debug("Using include patterns {}", includes);
        String[] excludes = getPatterns(node.getChildren("exclude"));
        logger.debug("Using exclude patterns {}", excludes);
        
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(base);
        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        
        return ds;
    }
    
    static String[] getPatterns(List<DataNode> nodes) {
        String[] ps = new String[nodes.size()];
        int i = 0;
        for (DataNode node: nodes) {
            ps[i] = node.getValue();
            i++;
        }
        return ps;
    }
}
