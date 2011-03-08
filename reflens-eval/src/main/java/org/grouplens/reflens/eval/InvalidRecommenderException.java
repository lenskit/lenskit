/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.eval;

import java.io.File;
import java.net.URI;

import javax.annotation.Nullable;

/**
 * Raised if the recommender cannot be created for some reason.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@SuppressWarnings("serial")
public class InvalidRecommenderException extends Exception {
	private final @Nullable URI sourceUri;

	/**
	 * 
	 */	
	public InvalidRecommenderException() {
		sourceUri = null;
	}

	/**
	 * @param message
	 */
	public InvalidRecommenderException(String message) {
		super(message);
		sourceUri = null;
	}
	
	public InvalidRecommenderException(URI uri, String message) {
		super(message);
		sourceUri = uri;
	}

	/**
	 * @param cause
	 */
	public InvalidRecommenderException(Throwable cause) {
		super(cause);
		sourceUri = null;
	}
	
	public InvalidRecommenderException(URI uri, Throwable cause) {
		super(cause);
		sourceUri = uri;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidRecommenderException(String message, Throwable cause) {
		super(message, cause);
		sourceUri = null;
	}
	
	public InvalidRecommenderException(URI uri, String message, Throwable cause) {
		super(message, cause);
		sourceUri = uri;
	}

	public @Nullable URI getSourceUri() {
		return sourceUri;
	}
	
	@Override
	public String getMessage() {
		String msg = super.getMessage();
		if (sourceUri != null) {
			URI base = new File("").toURI();
			URI simple = base.relativize(sourceUri);
			msg += "\nEncountered in " + simple.toString();
		}
		return msg;
	}
}
