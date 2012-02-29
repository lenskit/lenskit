/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.lenskit.core.Builder;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;

/**
 * A component type annotated with @Built will be automatically built by a
 * {@link Builder} when being instantiated by a
 * {@link LenskitRecommenderEngineFactory}. It will be created during the
 * "build" phase and the instance will be used to resolve all dependencies when
 * creating Recommenders from the built RecommenderEngine.
 * <p>
 * The factory will automatically identify the type of Builder if a Builder was
 * not manually configured for the component type. It first checks if the type
 * has been annotated with @DefaultBuilder; then it looks for a static inner
 * class implementing {@link Builder}; finally it looks for a Builder
 * implementation in the same package named <i>ComponentName</i>Builder
 * <p>
 * Because {@link LenskitRecommenderEngine} uses default object serialization to
 * store its state on disk, it is strongly recommended that types annotated with @Built
 * implement Serializable or Externalizable.
 *
 * @author Michael Ludwig
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Built {
    /**
     * Return <tt>true</tt> if the built instance should not be stored in the
     * RecommenderEngine, and is only needed for resolving dependencies during
     * the build phase. If <tt>true</tt>, built instances will not be retained
     * directly in the recommender container (although they may be referenced by
     * objects which are retained).
     *
     * @return <tt>true</tt> if the built instance is used only in the build
     *         process.
     */
    boolean ephemeral() default false;
}
