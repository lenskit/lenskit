/**
 * Meta-annotations for describing recommender parameters.
 * <p>
 * RefLens makes heavy use of annotations to describe parameters to allow Guice
 * to be used to provide parameter values.  The annotations in this package are
 * intended to be applied to such annotations and are used by the default Guice
 * module logic to set up default values and read parameters from Java properties.
 * See {@link org.grouplens.reflens.RecommenderModule}'s protected support methods
 * for information on how these annotations can be used.
 */
package org.grouplens.reflens.params.meta;