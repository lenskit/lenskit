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
package org.lenskit;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Get access to general information about LensKit.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class LenskitInfo {
    private static final Logger logger = LoggerFactory.getLogger(LenskitInfo.class);
    private static SoftReference<Set<String>> revisionSet;

    private LenskitInfo() {
    }

    private static Set<String> loadRevisionSet() {
        ImmutableSet.Builder<String> revisions = ImmutableSet.builder();
        InputStream input = LenskitInfo.class.getResourceAsStream("/META-INF/lenskit/git-commits.lst");
        if (input != null) {
            try (Reader reader = new InputStreamReader(input, Charsets.UTF_8);
                 BufferedReader lines = new BufferedReader(reader)) {
                String line;
                while ((line = lines.readLine()) != null) {
                    revisions.add(StringUtils.trim(line));
                }
            } catch (IOException e) {
                logger.warn("Could not read Git revision list", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("error closing git-commit list", e);
                }
            }
        } else {
            logger.warn("cannot find LensKit revision list");
        }
        Set<String> revset = revisions.build();
        logger.debug("have {} active revisions", revset.size());
        return revset;
    }

    /**
     * Get the set of revisions LensKit is built from.  This is in the order returned by {@code git log},
     * so the head revision is first.
     * @return The set of revisions included in this build of LensKit.
     */
    @Nonnull
    public static synchronized Set<String> getRevisions() {
        Set<String> revisions = revisionSet == null ? null : revisionSet.get();
        if (revisions == null) {
            revisions = loadRevisionSet();
            revisionSet = new SoftReference<>(revisions);
        }
        return revisions;
    }

    /**
     * Query whether this version of LensKit includes a particular revision.
     *
     * @param revision The revision to query.
     * @return {@code true} if the LensKit source is descended from {@code revision}.
     */
    public static boolean includesRevision(String revision) {
        return getRevisions().contains(revision);
    }

    /**
     * Get the HEAD revision from which LensKit was built.
     * @return The revision from which this version of LensKit was built.
     */
    @Nonnull
    public static String getHeadRevision() {
        Iterator<String> iter = getRevisions().iterator();
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Get the current LensKit version.
     * @return The LensKit version.
     */
    @Nonnull
    public static String lenskitVersion() {
        Properties props = new Properties();
        try (InputStream stream = LenskitInfo.class.getResourceAsStream("/META-INF/lenskit/version.properties")) {
            props.load(stream);
        } catch (IOException e) {
            logger.warn("could not load LensKit version properties", e);
        }
        return props.getProperty("lenskit.version", "UNKNOWN");
    }
}
