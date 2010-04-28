/**
 * 
 */
package org.grouplens.reflens.data.generic;

import java.util.Map;

import org.grouplens.reflens.data.UserHistory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GenericUserHistory<U, I> extends GenericRatingVector<I> implements
		UserHistory<U, I> {
	protected final U user;

	/**
	 * Create a new, empty user history.
	 */
	public GenericUserHistory(U user) {
		super();
		this.user = user;
	}
	
	public GenericUserHistory(U user, Map<I,Float> ratings) {
		super(ratings);
		this.user = user;
	}
	
	protected GenericUserHistory(MapFactory<I,Float> factory, U user, Map<I,Float> ratings) {
		super(factory, ratings);
		this.user = user;
	}
	
	@Override
	public U getUser() {
		return user;
	}
	
	@Override
	public GenericUserHistory<U,I> copy() {
		return new GenericUserHistory<U,I>(user, ratings);
	}
}
