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

package org.grouplens.reflens;

import static org.grouplens.reflens.params.meta.Parameters.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.params.MaxRating;
import org.grouplens.reflens.params.MeanDamping;
import org.grouplens.reflens.params.MinRating;
import org.grouplens.reflens.params.ThreadCount;
import org.grouplens.reflens.params.meta.Parameter;
import org.grouplens.reflens.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import com.google.inject.util.Providers;

/**
 * Base module for configuring Guice to inject recommenders.
 * 
 * This module provides the feature of automatically setting default values for
 * members.  For every field, including private fields, annotated with a parameter
 * annotation which contains a public static variable <var>DEFAULT_VALUE</var>,
 * the field is set to contain the value of <var>DEFAULT_VALUE</var>.
 * Parameter annotations are annotations which are themselves annotated with
 * {@link Parameter}.  See {@link MinRating} for an example parameter annotation.
 * 
 * @todo Document this module.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderModule extends AbstractModule {
	/**
	 * A logger, initialized for the class.  Provided here so subclasses don't
	 * have to include the boilerplate of creating their own loggers.
	 */
	private Logger logger;
	
	private String name;
	private @ThreadCount int threadCount = Runtime.getRuntime().availableProcessors();
	private @MeanDamping double damping;
	private @MinRating double minRating;
	private @MaxRating double maxRating;
	private @BaselinePredictor @Nullable Class<? extends RatingPredictor> baseline;
	private @ConstantPredictor.Value double constantBaselineValue;
	
	public RecommenderModule() {
		setName("<unnamed>");
		initializeDefaultValues(this.getClass());
		baseline = null;
	}
	
	private void initializeDefaultValues(Class<?> clazz) {
		Class<?> parent = clazz.getSuperclass();
		if (parent != null)
			initializeDefaultValues(parent);
		
		for (Field f: clazz.getDeclaredFields()) {
			f.setAccessible(true);
			Class<?> fType = f.getType();
			for (Annotation a: f.getAnnotations()) {
				Class<? extends Annotation> aType = a.annotationType();
				if (aType.isAnnotationPresent(Parameter.class)) {
					try {
						if (fType.equals(double.class) && hasDefaultDouble(aType)) {
							f.set(this, getDefaultDouble(aType));
						} else if (fType.equals(int.class) && hasDefaultInt(aType)) {
							f.set(this, getDefaultInt(aType));
						} else if (fType.equals(Class.class) && hasDefaultClass(aType)) {
							f.set(this, getDefaultClass(aType));
						}
					} catch (IllegalAccessException e) {
						logger.warn("Cannot set default value for {}", f.getName(), e);
					}
				}
			}
		}
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		logger.debug("Configuring recommender module");
		install(ThrowingProviderBinder.forModule(this));
		configureBaseline();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		logger = LoggerFactory.getLogger(this.getClass().getName() + ":" + name);
	}
	
	@Provides @ThreadCount
	public int getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(int count) {
		threadCount = count;
	}
	
	/**
	 * @return the damping
	 */
	@Provides @MeanDamping
	public double getDamping() {
		return damping;
	}

	/**
	 * @param damping the damping to set
	 */
	public void setDamping(double damping) {
		this.damping = damping;
	}
	
	/**
	 * @return the minRating
	 */
	@Provides @MinRating
	public double getMinRating() {
		return minRating;
	}

	/**
	 * @param minRating the minRating to set
	 */
	public void setMinRating(double minRating) {
		this.minRating = minRating;
	}

	/**
	 * @return the maxRating
	 */
	@Provides @MaxRating
	public double getMaxRating() {
		return maxRating;
	}

	/**
	 * @param maxRating the maxRating to set
	 */
	public void setMaxRating(double maxRating) {
		this.maxRating = maxRating;
	}
	
	/**
	 * Configure the binding for the baseline predictor.
	 * 
	 * @todo Make this capable of reifying generic types with
	 * {@link TypeUtils#reifyType(Type, Class)}.
	 */
	protected void configureBaseline() {
		LinkedBindingBuilder<RatingPredictor> binder = bind(RatingPredictor.class).annotatedWith(BaselinePredictor.class);
		if (baseline == null)
			binder.toProvider(Providers.of((RatingPredictor) null));
		else
			binder.to(baseline);
	}
	
	public Class<? extends RatingPredictor> getBaseline() {
		return baseline;
	}
	
	/**
	 * Set the predictor used for the baseline.
	 * @todo Support setting baseline providers.
	 * @param cls The class.
	 */
	public void setBaseline(Class<? extends RatingPredictor> cls) {
		baseline = cls;
	}
	
	@Provides @ConstantPredictor.Value
	public double getConstantBaselineValue() {
		return constantBaselineValue;
	}
	
	public void setConstantBaselineValue(double v) {
		constantBaselineValue = v;
	}
}
