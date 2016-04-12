package org.lenskit.featurize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {
    final private Map<String, List<String>> catAttrs = new HashMap<>();
    final private Map<String, Double> numAttrs = new HashMap<>();

    public Entity() {}

    public void setCatAttr(String name, List<String> values) {
        List<String> vals = new ArrayList<>(values);
        catAttrs.put(name, vals);
    }

    public void setNumAttr(String name, double value) {
        numAttrs.put(name, value);
    }

    public List<String> getCatAttr(String name) {
        List<String> vals = new ArrayList<>(catAttrs.get(name));
        return vals;
    }

    public double getNumAttr(String name) {
        return numAttrs.get(name);
    }
}
