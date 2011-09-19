package org.grouplens.lenskit.dtree;


/**
 * Helper methods for data trees.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Trees {
    private Trees() {}
    
    public static String childValue(DataNode node, String name) {
        for (DataNode n: node.getChildren()) {
            if (n.getName().equals(name)) {
                return n.getValue();
            }
        }
        return null;
    }
}
