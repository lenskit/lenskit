/**
 * 
 */
package org.grouplens.reflens.bench;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.grouplens.reflens.data.AbstractCursor;
import org.grouplens.reflens.data.AbstractRatingDataSource;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source backed by a simple delimited file.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleFileDataSource extends AbstractRatingDataSource {
	private static final Logger logger = LoggerFactory.getLogger(SimpleFileDataSource.class);
	private final File file;
	private final Pattern splitter;
	
	public SimpleFileDataSource(File file, String delimiter) throws FileNotFoundException {
		this.file = file;
		if (!file.exists())
			throw new FileNotFoundException(file.toString());
		splitter = Pattern.compile(Pattern.quote(delimiter));
	}
	
	public SimpleFileDataSource(File file) throws FileNotFoundException {
		this(file, System.getProperty("org.grouplens.reflens.bench.SimpleFileDataSource.delimiter", "\t"));
	}
	
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getRatings()
	 */
	@Override
	public Cursor<Rating> getRatings() {
		Scanner scanner;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return new RatingScannerCursor(scanner);
	}

	private class RatingScannerCursor extends AbstractCursor<Rating> {
		private Scanner scanner;
		private int lineno;
		
		public RatingScannerCursor(Scanner s) {
			lineno = 0;
			scanner = s;
		}

		@Override
		public void close() {
			if (scanner != null)
				scanner.close();
			scanner = null;
		}

		@Override
		public boolean hasNext() {
			return scanner != null && scanner.hasNextLine();
		}

		@Override
		public Rating next() {
			if (scanner == null) throw new NoSuchElementException();
			
			Rating ret = null;
			while (ret == null) {
				String line = scanner.nextLine();
				lineno += 1;
				String[] fields = splitter.split(line);
				if (fields.length < 3) {
					logger.error("Invalid input at {} line {}, skipping",
							file, lineno);
					continue;
				}
				long uid = Long.parseLong(fields[0]);
				long iid = Long.parseLong(fields[1]);
				double rating = Double.parseDouble(fields[2]);
			
				ret = new Rating(uid, iid, rating);
			}
			return ret;
		}		
		
	}
}
