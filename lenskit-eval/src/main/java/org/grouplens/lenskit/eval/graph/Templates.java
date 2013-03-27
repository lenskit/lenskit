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
