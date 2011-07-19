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
package org.grouplens.lenskit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.pico.JustInTimePicoContainer;
import org.grouplens.lenskit.pico.ParameterAnnotationInjector;
import org.picocontainer.ComponentFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitRecommenderEngineFactory} to set up recommender sessions.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @see LenskitRecommenderEngineFactory
 * @see LenskitRecommender
 */
public class LenskitRecommenderEngine implements RecommenderEngine {
    private final PicoContainer recommenderContainer;
    private final Map<Object, Object> sessionBindings;
    private final DAOFactory factory;
    
    public LenskitRecommenderEngine(DAOFactory factory,
                                    PicoContainer recommenderContainer, Map<Object, Object> sessionBindings) {
        this.factory = factory;
        this.recommenderContainer = recommenderContainer;
        
        // clone session binding into a HashMap so that we know its Serializable
        this.sessionBindings = new HashMap<Object, Object>(sessionBindings);
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized
     * engine from the given file. The new engine will be identical to the old
     * except it will use the new DAOFactory. It is assumed that the file was
     * created by using {@link #write(File)}.
     * 
     * @param factory
     * @param file
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public LenskitRecommenderEngine(DAOFactory factory,
                                    File file) throws IOException, ClassNotFoundException {
        this.factory = factory;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        try {
            recommenderContainer = (PicoContainer) in.readObject();
            sessionBindings = (Map<Object, Object>) in.readObject();
        } finally {
            in.close();
        }
    }

    /**
     * Write the state of this LenskitRecommenderEngine to the given file so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has a PicoContainer or
     * session bindings containing non-serializable types, this will fail.
     * 
     * @see #LenskitRecommenderEngine(DAOFactory, File)
     * @param file
     * @throws IOException
     */
    public void write(@Nonnull File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        try {
            out.writeObject(recommenderContainer);
            out.writeObject(sessionBindings);
        } finally {
            out.close();
        }
    }

    @Override
    public LenskitRecommender open() {
        if (factory == null)
            throw new IllegalStateException("No DAO creator supplied");
        DataAccessObject dao = factory.create();
        try {
            return open(dao, true);
        } catch (RuntimeException e) {
            dao.close();
            throw e;
        }
    }

    @Override
    public LenskitRecommender open(@Nonnull DataAccessObject dao, boolean shouldClose) {
        if (dao == null)
            throw new IllegalArgumentException("Cannot open with null DAO");
        return new LenskitRecommender(createSessionContainer(dao), dao, shouldClose);
    }
    
    private PicoContainer createSessionContainer(DataAccessObject dao) {
        ComponentFactory factory = new Caching().wrap(new ParameterAnnotationInjector.Factory());
        MutablePicoContainer sessionContainer = new JustInTimePicoContainer(factory, 
                                                                            recommenderContainer);
        // Configure session container
        for (Entry<Object, Object> binding: sessionBindings.entrySet()) {
            sessionContainer.addComponent(binding.getKey(), binding.getValue());
        }
        
        // Add in the dao
        sessionContainer.addComponent(dao);
        return sessionContainer;
    }
}