/**
 * 
 */
package org.grouplens.reflens.util;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Filter<T> {
	boolean apply(T obj);
}
