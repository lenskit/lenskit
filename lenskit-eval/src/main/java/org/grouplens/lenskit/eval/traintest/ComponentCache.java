/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.lenskit.inject.StaticInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Shared cache for components in merged compilations.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
class ComponentCache {
    private static final Logger logger = LoggerFactory.getLogger(ComponentCache.class);

    @Nullable
    private final File cacheDir;
    /**
     * Map of nodes to their UUIDs, used for the disk-based cache.
     */
    private final Map<DAGNode<CachedSatisfaction,DesireChain>,UUID> keyMap;
    /**
     * In-memory cache of shared components.
     */
    private final Cache<DAGNode<CachedSatisfaction,DesireChain>,Object> objectCache;

    /**
     * Construct a new component cache.
     *
     * @param dir The cache directory (or {@code null} to disable disk-based caching).
     */
    public ComponentCache(@Nullable File dir) {
        cacheDir = dir;
        keyMap = Maps.newHashMap();
        objectCache = CacheBuilder.newBuilder()
                                  .softValues()
                                  .build();
    }

    @Nullable
    public File getCacheDir() {
        return cacheDir;
    }

    public UUID getKey(DAGNode<CachedSatisfaction,DesireChain> node) {
        synchronized (keyMap) {
            UUID key = keyMap.get(node);
            if (key == null) {
                key = UUID.randomUUID();
                keyMap.put(node, key);
            }
            return key;
        }
    }

    public Function<DAGNode<CachedSatisfaction,DesireChain>,Object> makeInstantiator(DAGNode<CachedSatisfaction,DesireChain> graph) {
        return new Instantiator(new StaticInjector(graph));
    }

    private class Instantiator implements Function<DAGNode<CachedSatisfaction,DesireChain>,Object> {
        private final StaticInjector injector;

        public Instantiator(StaticInjector inj) {
            injector = inj;
        }

        @Nullable
        @Override
        public Object apply(@Nullable DAGNode<CachedSatisfaction, DesireChain> node) {
            Preconditions.checkNotNull(node, "input node");
            assert node != null;

            // shortcut - if it has an instance, don't bother caching
            Satisfaction sat = node.getLabel().getSatisfaction();
            if (sat.hasInstance()) {
                return injector.apply(node);
            }

            try {
                return objectCache.get(node, new NodeInstantiator(injector, node));
            } catch (ExecutionException e) {
                if (e.getCause() instanceof NullComponentException) {
                    return null;
                }
                throw Throwables.propagate(e.getCause());
            } catch (UncheckedExecutionException e) {
                if (e.getCause() instanceof NullComponentException) {
                    return null;
                }
                throw Throwables.propagate(e.getCause());
            }
        }
    }

    private class NodeInstantiator implements Callable<Object> {
        private final Function<DAGNode<CachedSatisfaction, DesireChain>, Object> delegate;
        private final DAGNode<CachedSatisfaction,DesireChain> node;

        public NodeInstantiator(Function<DAGNode<CachedSatisfaction,DesireChain>,Object> dlg,
                                DAGNode<CachedSatisfaction,DesireChain> n) {
            delegate = dlg;
            node = n;
        }

        @Override
        public Object call() throws IOException, NullComponentException {
            File cacheFile = null;
            if (cacheDir != null) {
                UUID key = getKey(node);
                cacheFile = new File(cacheDir, key.toString() + ".dat.gz");
                if (cacheFile.exists()) {
                    logger.debug("reading object for {} from cache (UUID {})",
                                 node.getLabel().getSatisfaction(), key);
                    return readCompressedObject(cacheFile, node.getLabel().getSatisfaction().getErasedType());
                }
            }

            // No object from the serialization stream, let's try to make one
            logger.debug("instantiating object for {}", node.getLabel().getSatisfaction());
            Object obj = delegate.apply(node);
            if (obj == null) {
                throw new NullComponentException();
            }

            // now save it to disk, if possible and non-null
            if (obj instanceof Serializable) {
                if (cacheFile != null) {
                    logger.debug("writing object {} to cache (UUID {})",
                                 obj, getKey(node));
                    writeCompressedObject(cacheFile, obj);
                }
            } else {
                logger.warn("unserializable object {} instantiated", obj);
            }

            return obj;
        }

        private void writeCompressedObject(File cacheFile, Object obj) throws IOException {
            Files.createParentDirs(cacheFile);
            Closer closer = Closer.create();
            try {
                OutputStream out = closer.register(new FileOutputStream(cacheFile));
                OutputStream gzOut = closer.register(new GZIPOutputStream(out));
                ObjectOutputStream objOut = closer.register(new ObjectOutputStream(gzOut));
                objOut.writeObject(obj);
            } catch (Throwable th) {
                throw closer.rethrow(th);
            } finally {
                closer.close();
            }
        }

        private Object readCompressedObject(File cacheFile, Class<?> type) throws IOException {
            // The file is there, copy it
            Closer closer = Closer.create();
            try {
                InputStream in = closer.register(new FileInputStream(cacheFile));
                InputStream gzin = closer.register(new GZIPInputStream(in));
                ObjectInputStream oin = closer.register(new ObjectInputStream(gzin));
                return type.cast(oin.readObject());
            } catch (Throwable th) {
                throw closer.rethrow(th);
            } finally {
                closer.close();
            }
        }
    }

    private static class NullComponentException extends Exception {
    }
}
