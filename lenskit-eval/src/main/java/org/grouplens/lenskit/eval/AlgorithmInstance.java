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
package org.grouplens.lenskit.eval;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of a recommender algorithm to be benchmarked.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    private @Nonnull String algoName;
    private @Nullable LenskitRecommenderEngineFactory factory;
    private @Nonnull Map<String,Object> attributes;
    private boolean preload = false;

    public AlgorithmInstance() {
        attributes = new HashMap<String,Object>();
        factory = new LenskitRecommenderEngineFactory();
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
     * Get the name of this algorithm.  This returns a short name which is
     * used to identify the algorithm or instance.
     * @return The algorithm's name
     */
    public String getName() {
        return algoName;
    }

    /**
     * Set the instance's name.
     * @param name The instance name
     */
    public void setName(String name) {
        algoName = name;
    }
    
    /**
     * Query whether this algorithm is to operate on in-memory data.
     * @return <tt>true</tt> if the ratings database should be loaded in-memory
     * prior to running.
     */
    public boolean getPreload() {
        return preload;
    }
    
    public void setPreload(boolean pl) {
        preload = pl;
    }

    public Map<String,Object> getAttributes() {
        return attributes;
    }

    public RecommenderEngineFactory getFactory() {
        return factory;
    }
    
    public void setFactory(LenskitRecommenderEngineFactory factory) {
        this.factory = factory;
    }

    public Recommender buildRecommender(RatingDataAccessObject dao, SharedRatingSnapshot sharedSnapshot) {
        if (factory == null)
            throw new IllegalStateException("no factory set");
        
        RecommenderEngine engine;
        synchronized (factory) {
            factory.bindDefault(RatingSnapshot.class, sharedSnapshot);
            try {
                engine = factory.create(dao);
            } finally {
                factory.bindDefault(RatingSnapshot.class, (RatingSnapshot) null);
            }
        }
        
        return engine.open(dao, false);
    }
    
    public static AlgorithmInstance load(File f) throws InvalidRecommenderException {
        return load(f, null);
    }

    public static AlgorithmInstance load(File f, @Nullable ClassLoader classLoader) throws InvalidRecommenderException {
        logger.info("Loading recommender definition from {}", f);
        String xtn = fileExtension(f);
        logger.debug("Loading recommender from {} with extension {}", f, xtn);
        if (classLoader == null)
            classLoader = Thread.currentThread().getContextClassLoader();
        ScriptEngineManager mgr = new ScriptEngineManager(classLoader);
        ScriptEngine engine = mgr.getEngineByExtension(xtn);
        if (engine == null)
            throw new InvalidRecommenderException(f.toURI(), "Cannot find engine for extension " + xtn);
        
        ScriptEngineFactory factory = engine.getFactory();
        logger.debug("Using {} {}", factory.getEngineName(), factory.getEngineVersion());
        AlgorithmInstance algo = new AlgorithmInstance();
        mgr.put("rec", algo);
        try {
            Reader r = new FileReader(f);
            try {
                engine.eval(r);
                // FIXME: The factory is always instantiated, we don't know if the build fails
                // until later when create() is called
                if (algo.getFactory() != null)
                    return algo;
                else
                    throw new InvalidRecommenderException(f.toURI(), "No recommender factory configured");
            } finally {
                r.close();
            }
        } catch (ScriptException e) {
            throw new InvalidRecommenderException(f.toURI(), e);
        } catch (IOException e) {
            throw new InvalidRecommenderException(f.toURI(), e);
        }
    }

    static String fileExtension(File f) {
        return fileExtension(f.getName());
    }
    static String fileExtension(String fn) {
        int idx = fn.lastIndexOf('.');
        if (idx >= 0) {
            return fn.substring(idx+1);
        } else {
            return "";
        }
    }
}
