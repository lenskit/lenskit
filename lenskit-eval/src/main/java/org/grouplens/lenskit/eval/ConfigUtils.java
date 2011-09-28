package org.grouplens.lenskit.eval;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.grouplens.lenskit.dtree.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for parsing configurations.
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
