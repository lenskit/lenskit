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
package org.grouplens.lenskit.dtree;

import java.util.regex.Pattern;


/**
 * Helper methods for data trees.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Trees {
    private Trees() {}
    
    public static DataNode child(DataNode node, String name) {
        for (DataNode n: node.getChildren()) {
            if (n.getName().equals(name))
                return n;
        }
        return null;
    }
    
    public static String childValue(DataNode node, String name) {
        return childValue(node, name, null);
    }
    
    public static String childValue(DataNode node, String name, String dft) {
        return childValue(node, name, dft, true);
    }
    
    public static String childValue(DataNode node, String name, String dft, boolean trimmed) {
        for (DataNode n: node.getChildren()) {
            if (n.getName().equals(name)) {
                if (trimmed)
                    return n.getValue();
                else
                    return n.getRawValue();
            }
        }
        return dft;
    }
    
    private static final Pattern truePattern = 
            Pattern.compile("^true|yes$", Pattern.CASE_INSENSITIVE);
    
    static boolean reMatches(Pattern pat, String st) {
        return pat.matcher(st).find();
    }
    
    static boolean stringToBool(String s) {
        if (reMatches(truePattern, s)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean childValueBool(DataNode node, String name, boolean dft) {
        String v = childValue(node, name);
        if (v == null) {
            return dft;
        } else {
            return stringToBool(v);
        }
    }
    
    public static int childValueInt(DataNode node, String name, int dft) {
        String v = childValue(node, name);
        if (v == null) {
            return dft;
        } else {
            return Integer.parseInt(v);
        }
    }
}
