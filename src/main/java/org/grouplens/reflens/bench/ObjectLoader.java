/**
 * 
 */
package org.grouplens.reflens.bench;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to make class loading easier.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ObjectLoader {
	private static Logger logger = LoggerFactory.getLogger(ObjectLoader.class);
	
	public static <T> Class<T> getClass(String name) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> factClass =
				(Class<T>) Class.forName(name);
			logger.debug("Loaded class {}", factClass.getName());
			return factClass;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}
	
	/**
	 * Cosntruct a new instance of the class named by <tt>name</tt>.
	 * @param <T> A supertype of the class to construct.
	 * @param name The name of the class to construct.
	 * @return A new instance of the class <tt>name</tt>.
	 */
	public static <T> T makeInstance(String name) {
		try {
			Class<T> factClass = getClass(name);
			Constructor<T> ctor = factClass.getConstructor();
			return ctor.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		}
	}
}
