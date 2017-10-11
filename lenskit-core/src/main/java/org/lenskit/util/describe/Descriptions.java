/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.describe;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Utility classes for {@link AbstractDescriptionWriter}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public final class Descriptions {
    static Logger logger = LoggerFactory.getLogger(Descriptions.class);

    /**
     * Cache random UUIDs for objects.  The objects are weakly referenced and identified by identity.
     */
    private static final LoadingCache<Object,UUID> RANDOM_KEY_MAP =
            CacheBuilder.newBuilder()
                        .weakKeys()
                        .build(new CacheLoader<Object, UUID>() {
                            @Override
                            public UUID load(Object key) throws Exception {
                                return UUID.randomUUID();
                            }
                        });

    private Descriptions() {}

    /**
     * Create a description writer that will compute a SHA1 hash.
     *
     * @return A description writer computing a SHA1 hash.
     */
    public static HashDescriptionWriter sha1Writer() {
        return hashWriter(Hashing.sha1());
    }

    /**
     * Create a description writer for a particular hash function.
     * @param func The hash function.
     * @return A description writer that computes a hash using {@code func}.
     */
    public static HashDescriptionWriter hashWriter(HashFunction func) {
        return new HashDescriptionWriter(func.newHasher());
    }

    /**
     * Construct a new description writer that outputs a string.
     * @return A string description writer.
     */
    public static StringDescriptionWriter stringWriter() {
        return new StringDescriptionWriter();
    }

    /**
     * Get a default describer.  This describer uses the following algorithm:
     * <ol>
     * <li>If the object is {@link Describable}, call its its {@link Describable#describeTo(DescriptionWriter)} method.</li>
     * <li>If the object is {@link java.io.Serializable}, write its class and a SHA1 checksum of
     * its serialized form to the description.</li>
     * <li>Otherwise, generate a random {@link java.util.UUID} as the object's description.  This
     * UUID is remembered, so the same object will have the same UUID within a single instance of
     * the JVM.</li>
     * </ol>
     *
     * @return A describer implementing the logic above.
     */
    public static Describer<Object> defaultDescriber() {
        return DefaultDescriber.INSTANCE;
    }

    private static enum DefaultDescriber implements Describer<Object> {
        INSTANCE;

        @Override
        public void describe(Object obj, DescriptionWriter description) {
            if (obj == null) {
                description.putField("type", "null");
                return;
            }
            description.putField("type", obj.getClass().getName());
            if (obj instanceof String) {
                // FIXME Do something saner here - just write the string somehow
                description.putField("string", (String) obj);
            } else if (obj instanceof Number) {
                description.putField("number", obj.toString());
            } if (obj instanceof Describable) {
                ((Describable) obj).describeTo(description);
            } else if (obj instanceof Serializable) {
                logger.debug("describing {} by hashing its serialization", obj);
                HashCode hash = Hashing.sha1().hashObject(obj, SerializeFunnel.INSTANCE);
                description.putField("hash", hash.toString());
            } else {
                logger.warn("object {} not describable or serializable, using nondeterministic key",
                            obj);
                UUID key = RANDOM_KEY_MAP.getUnchecked(obj);
                description.putField("_uuid", key.toString());
            }
        }
    }

    private static enum SerializeFunnel implements Funnel<Object> {
        INSTANCE;

        @Override
        public void funnel(Object from, PrimitiveSink into) {
            try (ObjectOutputStream out = new ObjectOutputStream(Funnels.asOutputStream(into))) {
                out.writeObject(from);
            } catch (IOException ex) {
                throw Throwables.propagate(ex);
            }
        }
    }
}
