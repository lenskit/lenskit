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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.grouplens.lenskit.dtree.xml.XMLTestCase;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestTrees extends XMLTestCase {

    @Test
    public void testCVNoChildren() throws SAXException, IOException {
        DataNode node = parse("<config/>");
        assertThat(Trees.childValue(node, "foo"), nullValue());
    }
    
    @Test
    public void testCVNoSuchChild() throws SAXException, IOException {
        DataNode node = parse("<config><foo>bar</foo></config>");
        assertThat(Trees.childValue(node, "spam"), nullValue());
    }
    
    @Test
    public void testCVChild() throws SAXException, IOException {
        DataNode node = parse("<config><foo>bar</foo></config>");
        assertThat(Trees.childValue(node, "foo"), equalTo("bar"));
    }
    
    @Test
    public void testToBoolTrue() {
        assertThat(Trees.stringToBool("true"), equalTo(true));
        assertThat(Trees.stringToBool("True"), equalTo(true));
        assertThat(Trees.stringToBool("tRUe"), equalTo(true));
        assertThat(Trees.stringToBool("yes"), equalTo(true));
        assertThat(Trees.stringToBool("YeS"), equalTo(true));
    }
    
    @Test
    public void testToBoolFalse() {
        assertThat(Trees.stringToBool("false"), equalTo(false));
        assertThat(Trees.stringToBool("false"), equalTo(false));
        assertThat(Trees.stringToBool("fAlSE"), equalTo(false));
        assertThat(Trees.stringToBool("no"), equalTo(false));
        assertThat(Trees.stringToBool("No"), equalTo(false));
        assertThat(Trees.stringToBool("bob"), equalTo(false));
    }
    
    @Test
    public void testCVChildTrue() throws SAXException, IOException {
        DataNode node = parse("<config><foo>true</foo></config>");
        assertThat(Trees.childValueBool(node, "foo", false), equalTo(true));
    }
    
    @Test
    public void testCVChildFalse() throws SAXException, IOException {
        DataNode node = parse("<config><foo>no</foo></config>");
        assertThat(Trees.childValueBool(node, "foo", true), equalTo(false));
    }
    
    @Test
    public void testCVChildBoolDft() throws SAXException, IOException {
        DataNode node = parse("<config><foo>no</foo></config>");
        assertThat(Trees.childValueBool(node, "bar", true), equalTo(true));
    }
}
