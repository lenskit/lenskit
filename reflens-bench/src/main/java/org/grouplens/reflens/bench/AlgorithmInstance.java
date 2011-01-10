package org.grouplens.reflens.bench;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.util.ObjectLoader;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * An instance of a recommender algorithm to be benchmarked.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class AlgorithmInstance {
	private final Properties algoProps; 
	private final String algoName;
	private final Injector injector;
	private final RecommenderEngineBuilder builder;
	
	public AlgorithmInstance(File specFile, Properties baseProps) throws InvalidRecommenderException {		
		algoProps = loadProperties(baseProps, specFile);
		
		String name = algoProps.getProperty("recommender.name");
		if (name == null) {
			name = fileBaseName(specFile, "properties");
		}
		algoName = name;
		
		injector = Guice.createInjector(getModule());
		try {
			builder = injector.getInstance(RecommenderEngineBuilder.class);
		} catch (CreationException e) {
			throw new InvalidRecommenderException("Error creating recommender builder", e);
		}
	}
	
	/**
	 * Helper method to extract the basename for a file with an expected extension.
	 * @param f The file
	 * @param xtn The extension (or NULL, if no extension is to be stripped).
	 * @return The basename of the file, with <var>xtn</var> stripped if it is
	 * the file's extension.
	 */
	static String fileBaseName(File f, String xtn) {
		String name = f.getName();
		if (xtn != null && name.endsWith("." + xtn)) {
			name = name.substring(0, name.length() - xtn.length() - 1);
		}
		return name;
	}
	
	/**
	 * Load the properties object for the specified file.
	 * @param base
	 * @param specFile
	 * @return
	 * @throws IOException
	 */
	private static Properties loadProperties(Properties base, File specFile) throws InvalidRecommenderException {
		if (base == null)
			base = System.getProperties();

		try {
			Reader reader = null;
			try {
				reader = new FileReader(specFile);
				Properties props = new Properties(base);
				props.load(reader);
				return props;
			} finally {
				if (reader != null)
					reader.close();
			}
		} catch (IOException e) {
			throw new InvalidRecommenderException(e);
		}
	}
	
	/**
	 * Get the name of this algorithm.  This returns a short name which is
	 * used to identify the algorithm or instance.
	 * @return The algorithm's name
	 */
	public String getName() {
		return algoName;
	}
	
	private Module getModule() throws InvalidRecommenderException {
		String modName = algoProps.getProperty("recommender.module");
		if (modName == null) {
			throw new InvalidRecommenderException("No recommender module specified");
		}
		try {
			return ObjectLoader.makeInstance(modName, Properties.class, algoProps);
		} catch (Exception e) {
			throw new InvalidRecommenderException("Error instantiating recommender module", e);
		}
	}
	
	public RecommenderEngineBuilder getBuilder() {
		return builder;
	}
}
