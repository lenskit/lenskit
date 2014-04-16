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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.lenskit.inject.GraphtUtils;
import org.grouplens.lenskit.inject.StaticInjector;
import org.grouplens.lenskit.util.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import java.io.*;
import java.util.List;
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
    
    @Nullable
    private final ClassLoader classLoader;
    
    private final LoadingCache<DAGNode<Component,Dependency>,String> keyCache;

    /**
     * In-memory cache of shared components.
     */
    private final Cache<DAGNode<Component,Dependency>,Optional<Object>> objectCache;

    /**
     * Construct a new component cache.
     *
     * @param dir The cache directory (or {@code null} to disable disk-based caching).
     * @param loader The class loader to be used when loading components from disk (or {@code null} if not needed)
     */
    public ComponentCache(@Nullable File dir, @Nullable ClassLoader loader) {
        cacheDir = dir;
        classLoader = loader;
        keyCache = CacheBuilder.newBuilder()
                               .weakKeys()
                               .build(CacheLoader.from(new NodeKeyGenerator()));
        objectCache = CacheBuilder.newBuilder()
                                  .weakKeys()
                                  .softValues()
                                  .build();
    }

    @SuppressWarnings("unused")
    @Nullable
    public File getCacheDir() {
        return cacheDir;
    }

    public String getKey(final DAGNode<Component,Dependency> node) {
        Preconditions.checkNotNull(node, "cached node");
        try {
            return keyCache.get(node);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    public Function<DAGNode<Component,Dependency>,Object> makeInstantiator(DAGNode<Component,Dependency> graph) {
        return new Instantiator(new StaticInjector(graph));
    }

    private class Instantiator implements Function<DAGNode<Component,Dependency>,Object> {
        private final StaticInjector injector;

        public Instantiator(StaticInjector inj) {
            injector = inj;
        }

        @Nullable
        @Override
        public Object apply(@Nullable DAGNode<Component, Dependency> node) {
            Preconditions.checkNotNull(node, "input node");
            assert node != null;

            // shortcut - if it has an instance, don't bother caching
            Satisfaction sat = node.getLabel().getSatisfaction();
            if (sat.hasInstance()) {
                logger.debug("{} already instantiated", sat);
                return injector.apply(node);
            }

            try {
                logger.debug("satisfying instantiation request for {}", sat);
                Optional<Object> result = objectCache.get(node, new NodeInstantiator(injector, node));
                return result.orNull();
            } catch (ExecutionException e) {
                Throwables.propagateIfPossible(e.getCause());
                throw new UncheckedExecutionException(e.getCause());
            } catch (UncheckedExecutionException e) {
                Throwables.propagateIfPossible(e.getCause());
                throw e;
            }
        }
    }

    private class NodeInstantiator implements Callable<Optional<Object>> {
        private final Function<DAGNode<Component, Dependency>, Object> delegate;
        private final DAGNode<Component,Dependency> node;

        public NodeInstantiator(Function<DAGNode<Component,Dependency>,Object> dlg,
                                DAGNode<Component,Dependency> n) {
            delegate = dlg;
            node = n;
        }

        @Override
        public Optional<Object> call() throws IOException {
            File cacheFile = null;
            if (cacheDir != null) {
                String key = getKey(node);
                cacheFile = new File(cacheDir, key + ".dat.gz");
                if (cacheFile.exists()) {
                    logger.debug("reading object for {} from cache (key {})",
                                 node.getLabel().getSatisfaction(), key);
                    Object obj = readCompressedObject(cacheFile, node.getLabel().getSatisfaction().getErasedType());
                    logger.debug("read object {} from key {}", obj, key);
                    return Optional.fromNullable(obj);
                }
            }

            // No object from the serialization stream, let's try to make one
            logger.debug("instantiating object for {}", node.getLabel().getSatisfaction());
            Optional<Object> result = Optional.fromNullable(delegate.apply(node));

            // now save it to disk, if possible and non-null
            if (result.isPresent()) {
                Object obj = result.get();
                if (obj instanceof Serializable) {
                    if (cacheFile != null) {
                        logger.debug("writing object {} to cache (key {})",
                                     obj, getKey(node));
                        if (logger.isDebugEnabled()) {
                            StringDescriptionWriter sdw = Descriptions.stringWriter();
                            NodeDescriber.INSTANCE.describe(node, sdw);
                            logger.debug("object description: {}", sdw.finish());
                        }
                        writeCompressedObject(cacheFile, obj);
                        logger.info("cached object {} as {} ({} bytes)",
                                    obj, getKey(node), cacheFile.length());
                    }
                } else {
                    logger.warn("unserializable object {} instantiated", result);
                }
            }

            return result;
        }

        private void writeCompressedObject(File cacheFile, Object obj) throws IOException {
            assert cacheDir != null;
            if (cacheDir.mkdirs()) {
                logger.debug("created cache directory {}", cacheDir);
            }
            StagedWrite stage = StagedWrite.begin(cacheFile);
            try {
                Closer closer = Closer.create();
                try {
                    OutputStream out = closer.register(stage.openOutputStream());
                    OutputStream gzOut = closer.register(new GZIPOutputStream(out));
                    ObjectOutputStream objOut = closer.register(new ObjectOutputStream(gzOut));
                    objOut.writeObject(obj);
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
                stage.commit();
            } finally {
                stage.close();
            }
        }

        private Object readCompressedObject(File cacheFile, Class<?> type) {
            // The file is there, load it
            try {
                Closer closer = Closer.create();
                try {
                    InputStream in = closer.register(new FileInputStream(cacheFile));
                    InputStream gzin = closer.register(new GZIPInputStream(in));
                    ObjectInputStream oin = closer.register(new CustomClassLoaderObjectInputStream(gzin, classLoader));
                    return type.cast(oin.readObject());
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
            } catch (IOException ex) {
                logger.warn("ignoring cache file {} due to read error: {}",
                            cacheFile.getName(), ex.toString());
                logger.info("This error can be caused by a corrupted cache file.");
                return null;
            }
        }
    }

    //region Node key generation

    /**
     * Function to generate a key for a graph node.
     */
    private class NodeKeyGenerator implements Function<DAGNode<Component, Dependency>, String> {
        @Nullable
        @Override
        public String apply(@Nullable DAGNode<Component, Dependency> node) {
            HashDescriptionWriter descr = Descriptions.sha1Writer();
            NodeDescriber.INSTANCE.describe(node, descr);
            return descr.finish().toString();
        }
    }

    /**
     * A describer for graph nodes, for generating keys of configuration subgraphs.
     */
    private static enum NodeDescriber implements Describer<DAGNode<Component, Dependency>> {
        INSTANCE;

        @Override
        public void describe(DAGNode<Component, Dependency> node, DescriptionWriter description) {
            node.getLabel().getSatisfaction().visit(new LabelDescriptionVisitor(description));
            description.putField("cachePolicy", node.getLabel().getCachePolicy().name());
            List<DAGNode<Component, Dependency>> edges =
                    Lists.transform(GraphtUtils.DEP_EDGE_ORDER.sortedCopy(node.getOutgoingEdges()),
                                    DAGEdge.<Component,Dependency>extractTail());
            description.putList("dependencies", edges, INSTANCE);
        }
    }

    /**
     * Satisfaction visitor for hashing node satisfactions (objects, etc.)
     */
    private static class LabelDescriptionVisitor implements SatisfactionVisitor<String> {
        private final DescriptionWriter description;

        public LabelDescriptionVisitor(DescriptionWriter sink) {
            description = sink;
        }

        @Override
        public String visitNull() {
            description.putField("type", "null");
            return "";
        }

        @Override
        public String visitClass(Class<?> clazz) {
            description.putField("type", "class")
                       .putField("class", clazz.getCanonicalName());
            return clazz.getCanonicalName();
        }

        @Override
        public String visitInstance(Object instance) {
            description.putField("type", "instance")
                       .putField("object", instance);
            return instance.toString();
        }

        @Override
        public String visitProviderClass(Class<? extends Provider<?>> pclass) {
            description.putField("type", "provider class")
                       .putField("class", pclass.getCanonicalName());
            return pclass.getCanonicalName();
        }

        @Override
        public String visitProviderInstance(Provider<?> provider) {
            if (provider == null) {
                description.putField("type", "null provider");
                return "null provider";
            } else {
                description.putField("type", "provider")
                           .putField("provider", provider);
                return provider.toString();
            }
        }
    }
    //endregion
}
