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
