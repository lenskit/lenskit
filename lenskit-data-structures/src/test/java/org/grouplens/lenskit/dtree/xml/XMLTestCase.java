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
package org.grouplens.lenskit.dtree.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class XMLTestCase {
    protected DocumentBuilder docBuilder;
    
    @Before
    public void setupDocBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        docBuilder = factory.newDocumentBuilder();
    }
    
    protected XMLDataNode parse(String text) throws SAXException, IOException {
        return parse(null, text);
    }
    
    protected XMLDataNode parse(Properties props, String text) throws SAXException, IOException {
        Reader reader = new StringReader(text);
        InputSource source = new InputSource(reader);
        return XMLDataNode.wrap(props, docBuilder.parse(source));
    }
}
