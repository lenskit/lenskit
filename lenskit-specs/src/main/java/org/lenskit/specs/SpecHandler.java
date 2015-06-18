package org.lenskit.specs;

/**
 * Resolve specifications into objects in an extensible fashion.  The heart of this interface is the
 * {@link #build(Class, AbstractSpec)}.  Instances of this interface are loaded via the {@link java.util.ServiceLoader}
 * facility, and queried in turn for one that can turn a particular specification into the requested type.  Handlers
 * should be selective in the types they handle, to minimize the chance of conflicts.
 *
 * @see SpecUtils#buildObject(Class, AbstractSpec)
 */
public interface SpecHandler {
    /**
     * Build an object from a specification.
     * @param type The type of object desired.
     * @param spec The specification.
     * @param <T> The type of object.
     * @return The object, or {@code null} if this handler cannot build an object.
     */
    <T> T build(Class<T> type, AbstractSpec spec);
}
