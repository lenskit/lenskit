package org.grouplens.reflens.data;

import java.io.IOException;

import com.google.inject.throwingproviders.CheckedProvider;

/**
 * Provider for data sources.
 * 
 * <p>Unfortunately, Guice does not provide support for dependency lifecycle
 * management, so client code will need to take care of making sure that data
 * sources are closed at the appropriate time.  This can be done via GuiceyFruit
 * or with custom providers that interact with surrounding close() code, possibly
 * using custom scopes.
 * 
 * <p>If you are using data sources within servlet requests, you can create a
 * request-scoped {@link DataSourceProvider} that exposes a <tt>close()</tt>
 * method.  This data source would open and memoize the data source on the first
 * call to its {@link #get()} method, and return the same source on subsequent
 * calls.  Then create a servlet filter that receives the data source provider
 * injected and instruct it to close the open data source (if one exists) after
 * the request has been finished.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DataSourceProvider<S extends DataSource> extends
		CheckedProvider<S> {
	/**
	 * Get an open data source.
	 * @return A data source ready for reading.
	 * @throws IOException if there is an error opening the data source.
	 */
	S get() throws IOException;
}
