/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.javadoc.taglets;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

/**
 * Taglet for referencing Trac bugs.
 *
 * @author Michael Ekstrand
 */
public class TracIssueTaglet implements Taglet {
    private static String TRAC_URL = "http://dev.grouplens.org/trac/lenskit/";

    private String tracUrl;

    /**
     * Register the extra inline taglet.
     * @param url The Trac base URL.
     */
    public TracIssueTaglet(String url) {
        tracUrl = url;
    }

    /**
     * Register the taglet.
     * @param map The taglet map to register in.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(Map map) {
        map.put("bug", new TracIssueTaglet(TRAC_URL));
    }

    @Override
    public boolean inField() {
        return true;
    }

    @Override
    public boolean inConstructor() {
        return true;
    }

    @Override
    public boolean inMethod() {
        return true;
    }

    @Override
    public boolean inOverview() {
        return true;
    }

    @Override
    public boolean inPackage() {
        return true;
    }

    @Override
    public boolean inType() {
        return true;
    }

    @Override
    public boolean isInlineTag() {
        return true;
    }

    @Override
    public String getName() {
        return "bug";
    }

    @Override
    public String toString(Tag tag) {
        StringBuilder bld = new StringBuilder();
        bld.append("<a href=\"")
           .append(tracUrl)
           .append("ticket/")
           .append(tag.text())
           .append("\">")
           .append("issue ")
           .append(tag.text())
           .append("</a>");
        return bld.toString();
    }

    @Override
    public String toString(Tag[] tags) {
        StringBuilder bld = new StringBuilder();
        for (Tag tag : tags) {
            bld.append(toString(tag));
        }
        return bld.toString();
    }
}
