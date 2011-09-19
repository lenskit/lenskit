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
}
