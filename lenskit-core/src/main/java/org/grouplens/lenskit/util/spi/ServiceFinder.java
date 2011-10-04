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
package org.grouplens.lenskit.util.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Find and select services by name. Uses a {@link ServiceLoader} to load
 * services from SPI definitions, and allows implementations to be selected by
 * configuration alias or class name.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class ServiceFinder<S> {
    static Map<Class<?>, ServiceFinder<?>> instanceMap =
            new HashMap<Class<?>, ServiceFinder<?>>();
    
    private static Logger logger = LoggerFactory.getLogger(ServiceFinder.class);
    
    /**
     * Create a new service finder for a particular interface.  Service finders
     * are cached by interface, so repeated queries for the same interface will
     * return the same instance.
     * 
     * @param iface The interface for the service.
     * @return A service finder capable of locating interfaces for this service.
     */
    public static <S> ServiceFinder<S> get(Class<S> iface) {
        synchronized (instanceMap) {
            @SuppressWarnings("unchecked")
            ServiceFinder<S> finder = (ServiceFinder<S>) instanceMap.get(iface);
            if (finder == null) {
                finder = new ServiceFinder<S>(iface, ServiceLoader.load(iface));
                instanceMap.put(iface, finder);
            }
            return finder;
        }
    }
    
    final @Nonnull Class<S> serviceInterface;
    final @Nonnull ServiceLoader<S> loader;
    
    private ServiceFinder(Class<S> iface, ServiceLoader<S> svcLoader) {
        serviceInterface = iface;
        loader = svcLoader;
    }
    
    /**
     * Get the underlying service loader.
     * 
     * @return The service loader used by this service finder.
     */
    public ServiceLoader<S> getLoader() {
        return loader;
    }
    
    public int getProviderCount() {
        return Iterables.size(loader);
    }
    
    /**
     * Find a service by name. This name can be a configurable alias (as in
     * {@link ConfigAlias}, a bare class name, or a fully-qualified class name.
     * Fully-qualified class names are not required to be registered via the SPI
     * — if no SPI-registered implementation is found with the specified name,
     * then this method attempts to load and instantiate the specified class.
     * 
     * @param name The name of the instance desired.
     * @return An instance of the service identified by <var>name</var>, or
     *         <tt>null</tt> if no implementation can be found.
     */
    @SuppressWarnings("unchecked")
    public S findProvider(@Nonnull String name) {
        S impl = null;
        
        for (S svc: loader) {
            ConfigAlias alias = svc.getClass().getAnnotation(ConfigAlias.class);
            if (alias != null && alias.value().equals(name)) {
                if (impl == null) {
                    logger.debug("Satisfying {}:{} with {}",
                                 new Object[]{serviceInterface, name, svc});
                    impl = svc;
                } else {
                    logger.warn("Multiple matches found for impl {} of {}",
                                name, serviceInterface);
                    logger.debug("New match: ", svc);
                }
            }
        }
        
        if (impl != null) return impl;
        
        // no config name, look for class names
        for (S svc: loader) {
            Class<?> cls = svc.getClass();
            String[] nameParts = cls.getName().split("\\.");
            if (nameParts[nameParts.length - 1].equals(name) || cls.getName().equals(name)) {
                if (impl == null) {
                    logger.debug("Satisfying {}:{} with {}",
                                 new Object[]{serviceInterface, name, svc});
                    impl = svc;
                } else {
                    logger.warn("Multiple matches found for impl {} of {}",
                                name, serviceInterface);
                    logger.debug("New match: ", svc);
                }
            }
        }
        
        if (impl != null) return impl;
        
        // nothing found via SPI, fall back to full load
        try {
            Class<?> cls = Class.forName(name);
            if (!serviceInterface.isAssignableFrom(cls)) {
                logger.warn("Found class {}, but not subclass of {}",
                            cls.getName(), serviceInterface.getName());
                return null;
            }
            
            return (S) cls.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            logger.error("Error instantiating service: " + e.getMessage(), e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("Error instantiating service: " + e.getMessage(), e);
            return null;
        }
    }
}
