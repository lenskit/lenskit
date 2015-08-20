package org.lenskit.util.keys;

/**
 * Interface for objects that can be identified by long key. This can be implemented by objects that have a natural
 * notion of a 'key', so that a separate key extractor is not required.
 */
public interface KeyedObject {
    /**
     * Get the key for this object.
     * @return A key identifying this object.
     */
    long getKey();
}
