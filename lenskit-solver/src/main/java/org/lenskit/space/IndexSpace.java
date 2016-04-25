package org.lenskit.space;

import java.io.Serializable;

public interface IndexSpace extends Serializable {
    void requestKeyMap(String name);
    int setKey(String name, Object key);
    boolean containsKey(String name, Object key);
    int getIndexForKey(String name, Object key);
}
