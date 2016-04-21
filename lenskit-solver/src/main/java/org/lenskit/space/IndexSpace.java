package org.lenskit.space;

public interface IndexSpace {
    void requestStringKeyMap(String name);
    void requestLongKeyMap(String name);
    int setLongKey(String name, long key);
    int setStringKey(String name, String key);
    boolean containsLongKey(String name, long key);
    boolean containsStringKey(String name, String key);
    int getIndexForLongKey(String name, long key);
    int getIndexForStringKey(String name, String key);
}
