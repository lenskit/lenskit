package org.lenskit.featurizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Entity {
    @JsonProperty
    final private Map<String, Set<String>> catAttrs = new HashMap<>();

    @JsonProperty
    final private Map<String, Double> numAttrs = new HashMap<>();

    public Entity() {}

    @JsonIgnore
    public Set<String> getCatAttrNames() {
        return catAttrs.keySet();
    }

    @JsonIgnore
    public Set<String> getNumAttrNames() {
        return numAttrs.keySet();
    }

    @JsonIgnore
    public void setCatAttr(String name, List<String> values) {
        Set<String> vals = new HashSet<>(values);
        catAttrs.put(name, vals);
    }

    public void addCatAttr(String name, String value) {
        if (catAttrs.containsKey(name)) {
            catAttrs.get(name).add(value);
        } else {
            Set<String> vals = new HashSet<>();
            vals.add(value);
            catAttrs.put(name, vals);
        }
    }

    public void addAllCatAttr(String name, List<String> values) {
        for (String value : values) {
            addCatAttr(name, value);
        }
    }

    @JsonIgnore
    public void setNumAttr(String name, double value) {
        numAttrs.put(name, value);
    }

    @JsonIgnore
    public List<String> getCatAttr(String name) {
        List<String> vals = new ArrayList<>(catAttrs.get(name));
        return vals;
    }

    @JsonIgnore
    public double getNumAttr(String name) {
        return numAttrs.get(name);
    }

    public boolean hasCatAttr(String name) {
        return catAttrs.containsKey(name);
    }

    public boolean hasNumAttr(String name) {
        return numAttrs.containsKey(name);
    }
}
