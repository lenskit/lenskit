/**
 *
 */
package org.grouplens.reflens.testing;

import java.util.Collection;
import java.util.Collections;

import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingCollectionDataSource;
import org.grouplens.reflens.data.RatingDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Base class providing facilities for doing tests against recommender Guice
 * modules.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class RecommenderModuleTest {
	/**
	 * Method to get the current recommender module under test.
	 * @return The module to be used, in conjunction with an empty rating set,
	 * for testing.
	 */
	protected abstract Module getModule();

	protected <T> T inject(Key<T> key) {
		Injector injector = Guice.createInjector(new AbstractModule() {
			protected void configure() {
			}
			@SuppressWarnings("unused")
			@Provides public RatingDataSource provideDataSource() {
				Collection<Rating> ratings = Collections.emptyList();
				return new RatingCollectionDataSource(ratings);
			}
		}, getModule());
		return injector.getInstance(key);
	}
}
