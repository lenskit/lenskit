package org.lenskit.gradle

import org.lenskit.specs.AbstractSpec

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Configurable wrapper for a specification.
 */
class SpecDelegate {
    final def AbstractSpec spec

    SpecDelegate(AbstractSpec sp) {
        spec = sp
    }

    @Override
    Object getProperty(String property) {
        return spec.metaClass.getProperty(spec, property)
    }

    @Override
    void setProperty(String property, Object newValue) {
        spec.metaClass.setProperty(spec, property, newValue)
    }

    @Override
    Object invokeMethod(String name, Object args) {
        try {
            return spec.metaClass.invokeMethod(spec, name, args)
        } catch (MissingMethodException ignored) {
            /* no method, continue with fallback code */
        }
        def argArray = args as Object[]
        if (argArray.length != 1) {
            throw missing(name, argArray, null)
        }
        def value = argArray[0]
        try {
            spec.metaClass.setProperty(spec, name, value)
        } catch (MissingPropertyException e) {
            // no such property
            throw missing(name, argArray, e)
        } catch (ClassCastException e) {
            // this means that we had the wrong type
            MetaProperty prop = spec.metaClass.hasProperty(spec, name)
            if (prop == null) {
                throw missing(name, argArray, e)
            }
            assert value != null // else how do we have a wrong class?
            def ptype = prop.type
            def vtype = value.class
            if (ptype == Path.class) {
                if (vtype == File.class) {
                    def file = value as File
                    prop.setProperty(spec, file.toPath())
                } else {
                    prop.setProperty(spec, Paths.get(value))
                }
            } else {
                throw missing(name, argArray, e);
            }
        }
        return null
    }

    private MissingMethodException missing(String name, Object[] args, Throwable cause) {
        def ex = new MissingMethodException(name, getClass(), args)
        if (cause != null) {
            if (ex.cause != null) {
                ex.initCause(cause)
            } else {
                ex.addSuppressed(cause)
            }
        }
        return ex
    }
}
