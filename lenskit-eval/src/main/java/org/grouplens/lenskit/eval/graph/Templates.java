/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.graph;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Helper class to load graph dumping templates.
 */
class Templates {
    private static final Logger logger = LoggerFactory.getLogger(Templates.class);
    static final Template graphTemplate;
    static final Template labelTemplate;

    static {
        graphTemplate = readTemplate("ConfigGraph.dot", false);
        labelTemplate = readTemplate("ComponentLabel.html", true);
    }

    private static Template readTemplate(String name, boolean escape) {
        Mustache.Compiler compiler = Mustache.compiler().escapeHTML(escape);
        InputStream istr = Templates.class.getResourceAsStream(name + ".mustache");
        if (istr == null) {
            throw new RuntimeException("cannot load template " + name);
        }
        Reader r;
        try {
            r = new InputStreamReader(istr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported", e);
        }
        try {
            return compiler.compile(r);
        } finally {
            try {
                r.close();
            } catch (IOException e) {
                logger.error("error closing template", e);
            }
        }
    }
}
