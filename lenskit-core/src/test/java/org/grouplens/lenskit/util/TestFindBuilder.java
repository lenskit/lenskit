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

package org.grouplens.lenskit.util;

import static org.junit.Assert.*;
import org.grouplens.lenskit.util.fbSamples.AbstractBuilder;
import org.grouplens.lenskit.util.fbSamples.SampleChildComponent;
import org.grouplens.lenskit.util.fbSamples.SampleChildComponentBuilder;
import org.grouplens.lenskit.util.fbSamples.SampleComponent;
import org.grouplens.lenskit.util.fbSamples.SampleComponent2;
import org.grouplens.lenskit.util.fbSamples.SampleComponent2Builder;
import org.grouplens.lenskit.util.fbSamples.SampleComponentBuilder;
import org.grouplens.lenskit.util.fbSamples.SampleComponentBuilderChild;
import org.grouplens.lenskit.util.fbSamples.SampleComponentBuilderChild2;
import org.grouplens.lenskit.util.fbSamples.SampleComponentInterface;
import org.grouplens.lenskit.util.fbSamples.SampleComponentNoBuilder;
import org.grouplens.lenskit.util.fbSamples.SampleImplementingComponentBuilder;
import org.grouplens.lenskit.util.fbSamples.SampleInnerComponentBuilder;
import org.junit.Test;

/**
 * Tests the <tt>isBuilderOf</tt> and <tt>findBuilder</tt> methods
 * of the ObjectLoader Class.
 */
public class TestFindBuilder {

	/**
	 * Tests the simplest use of the <tt>isBuilderOf</tt> method.
	 * The input builder is a builder of the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>true</tt>.
	 */ 
	@Test
	public void testSimpleCase() {
		assertTrue(ObjectLoader.isBuilderOf(SampleComponentBuilder.class,
				SampleComponent.class));		
	}

	/**
	 * Tests the use of the <tt>isBuilderOf</tt> method with a parent and child class.
	 * The input builder is a child of a builder of the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>true</tt>.
	 */
	@Test
	public void testChildClass() {
		assertTrue(ObjectLoader.isBuilderOf(SampleComponentBuilderChild.class,
				SampleComponent.class));
	}

	/**
	 * Tests the use of the <tt>isBuilderOf</tt> method with a parent and child class.
	 * The input builder is a child of a child of a builder of the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>true</tt>.
	 */
	@Test
	public void testChildClass2() {
		assertTrue(ObjectLoader.isBuilderOf(SampleComponentBuilderChild2.class, 
				SampleComponent.class));
	}

	/**
	 * Tests the <tt>isBuilderOf</tt> method involving an inner class.
	 * The input builder has an inner class that is the builder of the target type.
	 * However, the input class itself is <i>not</i> a builder of the target type.
	 * Therefore, the <tt>isBuilderOf</tt> method will return <tt>false</tt>.
	 */
	@Test
	public void testWrongBuilder() {
		assertFalse(ObjectLoader.isBuilderOf(SampleInnerComponentBuilder.class,
				SampleComponent.class));
	}
	

	/**
	 * Tests the <tt>isBuilderOf</tt> method with a mismatched builder and component.
	 * The input builder is a RecommenderComponentBuilder, but it is not compatible
	 * with the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>false</tt>.
	 */
	@Test
	public void testWrongBuilder2() {
		assertFalse(ObjectLoader.isBuilderOf(SampleComponentBuilder.class,
				SampleComponent2.class));
	}

	/**
	 * Tests the <tt>isBuilderOf</tt> method with another mismatched builder and component.
	 * The input builder is a child of a RecommenderComponentBuilder, but
	 * it is compatible with the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>false</tt>.
	 */
	@Test
	public void testWrongBuilder3() {
		assertFalse(ObjectLoader.isBuilderOf(SampleComponentBuilderChild.class,
				SampleComponent2.class));
	}

	/**
	 * Tests the <tt>isBuilderOf</tt> method with a non-builder class as input.
	 * The input builder class is not a RecommenderComponentBuilder.
	 * The <tt>isBuilderOf</tt> method should return <tt>false</tt>.
	 */
	@Test
	public void testWrongBuilder4() {
		assertFalse(ObjectLoader.isBuilderOf(ObjectLoader.class,SampleComponent.class));
	}

	/**
	 * Tests the <tt>isBuilderOf</tt> method with an abstract builder class as input.
	 * While the input class is a RecommenderComponentBuilder, it is abstract
	 * and therefore cannot be used to create components.
	 * The <tt>isBuilderOf</tt> method should return <tt>false</tt>.
	 */
	@Test
	public void testAbstractBuilder() {
		assertFalse(ObjectLoader.isBuilderOf(AbstractBuilder.class,SampleComponent.class));
	}

	/**
	 * Tests the <tt>isBuilderOf</tt> method with a builder of a child of the target type.
	 * As the builder produces a subclass of the target type, it is compatible
	 * with the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>true</tt>.
	 */
	@Test
	public void testBuilderOfChildClass() {
		assertTrue(ObjectLoader.isBuilderOf(SampleChildComponentBuilder.class,
				SampleComponent.class));
	}
	
	/**
	 * Tests the <tt>isBuilderOf</tt> method with a builder of a parent of the target type.
	 * The builder produces a superclass of the target type, and therefore is not
	 * compatible with the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>false</tt>.
	 */
	@Test
	public void testBuilderOfParentClass() {
		assertFalse(ObjectLoader.isBuilderOf(SampleComponentBuilder.class, 
				SampleChildComponent.class));
	}
	
	/**
	 * Tests the <tt>isBuilderOf</tt> method with a builder of a type that 
	 * implements the target type. The builder therefore creates objects 
	 * that are compatible with the target type.
	 * The <tt>isBuilderOf</tt> method should return <tt>true</tt>.
	 */
	@Test
	public void testBuilderOfSubInterface() {
		assertTrue(ObjectLoader.isBuilderOf(SampleImplementingComponentBuilder.class,
				SampleComponentInterface.class));
	}

	/**
	 * Tests the simplest use of the <tt>findBuilder</tt> method.
	 * The input builder class is a builder of the targetType.
	 * The method will return the originally-input builder class.
	 */
	@Test
	public void testFindSimpleCase() {
		assertEquals(ObjectLoader.findBuilder(SampleComponentBuilder.class,
				SampleComponent.class).getClass(),SampleComponentBuilder.class);
	}

	/**
	 * Tests the <tt>findBuilder</tt> method with a child of a builder of the target type as input.
	 * The builder class inherits a build method that is compatible with the target type.
	 * The <tt>findBuilder</tt> method will return the originally-input builder class.
	 */
	@Test
	public void testFindChildClass() {
		assertEquals(ObjectLoader.findBuilder(SampleComponentBuilderChild.class,
				SampleComponent.class).getClass(),SampleComponentBuilderChild.class);
	}

	/**
	 * Tests the <tt>findBuilder</tt> method with a child of a child of a builder of the
	 * target type as input.
	 * The builder class inherits a build method that is compatible with the target type.
	 * The <tt>findBuilder</tt> method will return the originally-input builder class.
	 */
	@Test
	public void testFindChildClass2() {
		assertEquals(ObjectLoader.findBuilder(SampleComponentBuilderChild2.class,
				SampleComponent.class).getClass(),SampleComponentBuilderChild2.class);
	}

	/**
	 * Tests the <tt>findBuilder</tt> method with a class that contains the proper builder as 
	 * an inner class as input.
	 * The <tt>findBuilder</tt> method will find and return this inner class.
	 */
	@Test
	public void testFindInnerClass() {
		assertEquals(ObjectLoader.findBuilder(SampleInnerComponentBuilder.class,
				SampleComponent.class).getClass(),SampleInnerComponentBuilder.ComponentBuilder.class);
	}

	/**
	 * Tests the <tt>findBuilder</tt> method's ability to find the proper builder by name.
	 * If some component class <tt>Foo</tt> is input, and there exists some class
	 *  <tt>FooBuilder</tt>, the <tt>findBuilder</tt> method will find and return 
	 *  this builder class, even if it wasn't passed as an argument to the method.
	 */
	@Test
	public void testFindByName() {
		assertEquals(ObjectLoader.findBuilder(SampleComponentBuilder.class,
				SampleComponent2.class).getClass(),SampleComponent2Builder.class);
	}

	/**
	 * Tests a failure of the <tt>findBuilder</tt> method, in which the targetType
	 *  argument has no builder. The <tt>findBuilder</tt> method should throw
	 *   an <tt>IllegalArgumentException</tt>.
	 */
	@Test
	public void testFailSimpleCase() {
		try {
			ObjectLoader.findBuilder(SampleComponentBuilder.class,
					SampleComponentNoBuilder.class);
			fail("IllegalArgumentException should be thrown.");
		}
		catch (IllegalArgumentException e) {}
	}

	/**
	 * Tests the <tt>findBuilder</tt> method when an abstract builder class is input.
	 * While the input class is technically a builder of the target type, it cannot
	 * be used because it is abstract.
	 * The <tt>findBuilder</tt> class should throw an <tt>IllegalArgumentException.</tt>
	 */
	@Test
	public void testFailAbstractClass() {
		try {
			ObjectLoader.findBuilder(AbstractBuilder.class, SampleComponentNoBuilder.class);
			fail("IllegalArgumentException should be thrown.");
		}
		catch (IllegalArgumentException e) {}	
	}

	/**
	 * Tests the <tt>findBuilder</tt> method where a class containing an inner class that is
	 * a builder of the target type is input. However, as this inner class is non-static,
	 * it cannot be used.
	 * The <tt>findBuilder</tt> method should throw an <tt>IllegalArgumentException</tt>.
	 */
	@Test
	public void testFailNonStaticInnerClass() {
		try {
			ObjectLoader.findBuilder(SampleInnerComponentBuilder.class,
					SampleComponentNoBuilder.class);
			fail("IllegalArgumentException should be thrown.");
		}
		catch (IllegalArgumentException e) {}
	}

	/**
	 * Tests the <tt>FindBuilder</tt> method where a child of the builder of the target type
	 * is input. As this builder inherits a build method that returns the target type,
	 * it is compatible and the <tt>FindBuilder</tt> method will return the input builder.
	 */
	@Test
	public void testFindBuilderOfChild() {
		assertEquals(ObjectLoader.findBuilder(SampleChildComponentBuilder.class,
				SampleComponent.class).getClass(), SampleChildComponentBuilder.class);
	}
	
	/**
	 * Tests the <tt>findBuilder</tt> method where a builder of a component that implements
	 * the target type is input. As the builder produces a component that is compatible with
	 * the target type, the <tt>findBuilder</tt> method should return the input builder.
	 */
	@Test
	public void testFindBuilderOfSubInterface() {
		assertEquals(ObjectLoader.findBuilder(SampleImplementingComponentBuilder.class,
				SampleComponentInterface.class).getClass(), 
				SampleImplementingComponentBuilder.class);
	}
}