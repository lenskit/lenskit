package org.grouplens.reflens.bench;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * Tests for the {@link AlgorithmInstance} class.  Not very extensive, but they
 * help us sanity-check a few things.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestAlgorithmInstance {
	@Test
	public void testBasename() {
		assertEquals("foo", AlgorithmInstance.fileBaseName(new File("foo"), null));
		assertEquals("foo", AlgorithmInstance.fileBaseName(new File("foo"), "bar"));
		assertEquals("foo.bar", AlgorithmInstance.fileBaseName(new File("foo.bar"), null));
		assertEquals("foo", AlgorithmInstance.fileBaseName(new File("foo.bar"), "bar"));
		assertEquals("foo.bar", AlgorithmInstance.fileBaseName(new File("foo.bar"), "properties"));
		assertEquals("foo", AlgorithmInstance.fileBaseName(new File("foo.properties"), "properties"));
		assertEquals("whizbang", AlgorithmInstance.fileBaseName(new File("whizbang.properties"), "properties"));
	}
}
