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
 * JavaDoc taglet for compatability information.
 *
 * @author Michael Ekstrand
 */
public class CompatTaglet implements Taglet {
    private String versionUrl;

    public CompatTaglet() {
        versionUrl = System.getProperty("grouplens.javadoc.versioning.url");
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public static void register(Map tagletMap) {
        Taglet tag = new CompatTaglet();
        tagletMap.put(tag.getName(), tag);
    }

    @Override
    public boolean inField() {
        return false;
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
        return false;
    }

    @Override
    public String getName() {
        return "compat";
    }

    @Override
    public String toString(Tag tag) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>");
        sb.append("<b>");
        if (versionUrl != null) {
            sb.append("<a href=\"");
            sb.append(versionUrl);
            sb.append("\">");
        }
        sb.append("Compatibility:");
        if (versionUrl != null) {
            sb.append("</a>");
        }
        sb.append("</b> ");
        sb.append(tag.text());
        sb.append("</p>");
        return sb.toString();
    }

    @Override
    public String toString(Tag[] tags) {
        if (tags.length == 1) {
            return toString(tags[0]);
        } else if (tags.length > 1) {
            throw new IllegalArgumentException("cannot have multiple @compat tags");
        } else {
            return "";
        }
    }
}
