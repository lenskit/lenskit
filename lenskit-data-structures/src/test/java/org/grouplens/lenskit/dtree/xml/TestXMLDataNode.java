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
package org.grouplens.lenskit.dtree.xml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Properties;

import org.grouplens.lenskit.dtree.DataNode;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestXMLDataNode extends XMLTestCase {
    Document simpleDoc;
    
    @Before
    public void createDocument() {
        simpleDoc = docBuilder.newDocument();
        simpleDoc.appendChild(simpleDoc.createElement("config"));
    }
    
    @Test
    public void testWrapNull() {
        assertThat(XMLDataNode.wrap(null), nullValue());
    }

    @Test
    public void testWrapElement() {
        DataNode node = XMLDataNode.wrap(simpleDoc.getDocumentElement());
        assertThat(node, notNullValue());
        assertThat(node.getName(), equalTo("config"));
        assertThat(node.getChildren(), Matchers.<DataNode>empty());
        assertThat(node.getValue(), equalTo(""));
    }
    
    @Test
    public void testAttribute() throws SAXException, IOException {
        DataNode node = XMLDataNode.wrap(simpleDoc.getDocumentElement());
        assertThat(node.getAttribute("foo"), nullValue());
        node = parse("<config foo=\"bar\"/>");
        assertThat(node.getAttribute("foo"), equalTo("bar"));
    }
    
    @Test
    public void testWrapDocument() {
        DataNode node = XMLDataNode.wrap(simpleDoc);
        assertThat(node, notNullValue());
        assertThat(node.getName(), equalTo("config"));
        assertThat(node.getChildren(), Matchers.<DataNode>empty());
        assertThat(node.getValue(), equalTo(""));
    }
    
    @Test
    public void testWrapContent() throws SAXException, IOException {
        DataNode node = parse("<content>READ ME</content>");
        assertThat(node, notNullValue());
        assertThat(node.getName(), equalTo("content"));
        assertThat( node.getChildren(), Matchers.<DataNode>empty());
        assertThat(node.getValue(), equalTo("READ ME"));
    }
    
    @Test
    public void testWrapChildren() throws SAXException, IOException {
        DataNode node = parse("<scroll>\n" +
        		"  <name>HACKEM MUCHE</name>\n" +
        		"  <type> identify </type>\n" +
        		"</scroll>");
        assertThat(node, notNullValue());
        assertThat(node.getName(), equalTo("scroll"));
        assertThat(node.getValue(), equalTo(""));
        
        assertThat(node.getChildren().size(), equalTo(2));
        assertThat(node.getChildren("foo"), Matchers.<DataNode>empty());
        assertThat(node.getChildren().get(0).getName(), equalTo("name"));
        assertThat(node.getChildren().get(0).getValue(), equalTo("HACKEM MUCHE"));
        assertThat(node.getChildren().get(1).getName(), equalTo("type"));
        assertThat(node.getChildren().get(1).getValue(), equalTo("identify"));
    }
    
    @Test
    public void testInterpolate() throws SAXException, IOException {
        Properties props = new Properties();
        props.setProperty("foo.bar", "flimflam");
        DataNode node = parse(props, "<scroll>pvalue: ${foo.bar}</scroll>");
        assertThat(node.getValue(), equalTo("pvalue: flimflam"));
    }
    
    @Test
    public void testInterpolateChild() throws SAXException, IOException {
        Properties props = new Properties();
        props.setProperty("foo.bar", "flimflam");
        DataNode node = parse(props, "<scroll><name>${foo.bar}</name></scroll>");
        assertThat(node.getChildren("name").get(0).getValue(),
                   equalTo("flimflam"));
    }
    
    @Test
    public void testInterpolateEscape() throws SAXException, IOException {
        Properties props = new Properties();
        props.setProperty("foo.bar", "$1\\t");
        DataNode node = parse(props, "<scroll><name>${foo.bar}</name></scroll>");
        assertThat(node.getChildren("name").get(0).getValue(),
                   equalTo("$1\\t"));
    }
   
    /**
     * When unignored, test that we can define properties in the properties
     * file.
     */
    @Ignore
    @Test
    public void testDefine() throws SAXException, IOException {
        Properties props = new Properties();
        props.setProperty("scroll.default", "FOOBIE BLETCH");
        DataNode node = parse(props,
                              "<scroll>\n" +
                              "  <property name='scroll.alt' value='READ ME'/>\n" +
                              "  <name>${scroll.default}</name>\n" +
                              "  <name>${scroll.alt}</name>\n" +
                              "</scroll>\n");
        assertThat(node.getChildren("name").get(0).getValue(),
                   equalTo("FOOBIE BLETCH"));
        assertThat(node.getChildren("name").get(1).getValue(),
                   equalTo("READ ME"));
    }
}
