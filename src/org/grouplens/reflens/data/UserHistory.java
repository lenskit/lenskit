package org.grouplens.reflens.data;

public interface UserHistory<U, I> extends RatingVector<I> {
	public U getUser();

	public float getAverageRating();
}
