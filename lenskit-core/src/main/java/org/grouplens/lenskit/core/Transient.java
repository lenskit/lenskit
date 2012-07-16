package org.grouplens.lenskit.core;

import org.grouplens.grapht.annotation.Attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a component dependency as transient. This is only done on provider/builder
 * components, and means that the specified dependency is only needed while the
 * output component is being built, but the final component does not depend on
 * the transient dependency.
 * <p>
 * Example: a provider that reads the ratings from the DAO to compute their average
 * and build a component around that average has a transient dependency on the DAO.
 *
 * @author Michael Ekstrand
 */
@Attribute
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
