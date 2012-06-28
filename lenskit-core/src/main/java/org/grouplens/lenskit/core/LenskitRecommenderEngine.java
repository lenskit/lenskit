/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceProvider;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

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
    private final Graph<Pair<Satisfaction, CachePolicy>, Desire> dependencies;
    private final Node<Pair<Satisfaction, CachePolicy>> rootNode;
    private final Node<Pair<Satisfaction, CachePolicy>> daoNode;
    
    private final InjectSPI spi;
    private final Map<Node<Pair<Satisfaction, CachePolicy>>, Object> sharedInstances;
    
    private final DAOFactory factory;

    LenskitRecommenderEngine(DAOFactory factory,
                             Graph<Pair<Satisfaction,CachePolicy>, Desire> dependencies,
                             Map<Node<Pair<Satisfaction, CachePolicy>>, Object> sharedInstances,
                             InjectSPI spi) {
        this.factory = factory;
        this.dependencies = dependencies;
        this.spi = spi;

        // clone session binding into a HashMap so that we know its Serializable
        this.sharedInstances = new HashMap<Node<Pair<Satisfaction, CachePolicy>>, Object>(sharedInstances);
        
        rootNode = dependencies.getNode(null);
        daoNode = dependencies.getNode(DAOSatisfaction.label());
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
            spi = (InjectSPI) in.readObject();
            dependencies = (Graph<Pair<Satisfaction,CachePolicy>, Desire>) in.readObject();
            sharedInstances = (Map<Node<Pair<Satisfaction,CachePolicy>>, Object>) in.readObject();
            rootNode = dependencies.getNode(null);
            daoNode = dependencies.getNode(DAOSatisfaction.label());
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
            out.writeObject(spi);
            out.writeObject(dependencies);
            out.writeObject(sharedInstances);
        } finally {
            out.close();
        }
    }

    @Override
    public LenskitRecommender open() {
        if (factory == null) {
            throw new IllegalStateException("No DAO creator supplied");
        }
        DataAccessObject dao = factory.create();
        try {
            return open(dao, true);
        } catch (RuntimeException e) {
            dao.close();
            throw e;
        }
    }

    /**
     * Open a recommender with a specific data connection. The client code must
     * close the recommender when it is finished with it.
     *
     * @param dao The DAO to connect the recommender to.
     * @param shouldClose If <tt>true</tt>, then the recommender should close the
     * DAO when it is closed.
     * @return A recommender ready for use and backed by <var>dao</var>.
     */
    public LenskitRecommender open(@Nonnull DataAccessObject dao, boolean shouldClose) {
        Preconditions.checkNotNull(dao, "Cannot open with null DAO");
        return new LenskitRecommender(new RecommenderInjector(dao), dao, shouldClose);
    }

    private class RecommenderInjector implements Injector {
        private final Map<Node<Pair<Satisfaction, CachePolicy>>, Object> newInstances;
        
        public RecommenderInjector(DataAccessObject dao) {
            newInstances = new HashMap<Node<Pair<Satisfaction, CachePolicy>>, Object>();
            newInstances.put(daoNode, dao);
        }
        
        @Override
        public <T> T getInstance(Class<T> type) {
            Desire d = spi.desire(null, type, true);
            Edge<Pair<Satisfaction, CachePolicy>, Desire> e = dependencies.getOutgoingEdge(rootNode, d);
            
            if (e != null) {
                // The type is one of the configured roots
                return this.<T>getInstance(e.getTail());
            } else {
                // The type is hopefully embedded in the graph
                for (Node<Pair<Satisfaction, CachePolicy>> n: dependencies.getNodes()) {
                    Pair<Satisfaction, CachePolicy> label = n.getLabel();
                    if (label != null && type.isAssignableFrom(label.getLeft().getErasedType())) {
                        // found a node capable of creating instances of type
                        return this.<T>getInstance(n);
                    }
                }
                return null;
            }
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private <T> T getInstance(final Node<Pair<Satisfaction, CachePolicy>> n) {
            Object session = newInstances.get(n);
            if (session != null) {
                return (T) session;
            }
            
            Object shared = sharedInstances.get(n);
            if (shared != null) {
                return (T) shared;
            }

            Pair<Satisfaction, CachePolicy> label = n.getLabel();
            assert label != null; // non-root edges don't have null labels
            Provider<?> provider = label.getLeft().makeProvider(new ProviderSource() {
                @Override
                public Provider<?> apply(Desire desire) {
                    Node<Pair<Satisfaction, CachePolicy>> d = dependencies.getOutgoingEdge(n, desire).getTail();
                    return new InstanceProvider(getInstance(d));
                }
            });
            
            T instance = (T) provider.get();
            newInstances.put(n, instance);
            return instance;
        }

        @Override
        public <T> T getInstance(Annotation qualifier, Class<T> type) {
            throw new UnsupportedOperationException();
        }
    }
}
