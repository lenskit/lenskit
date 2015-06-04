package org.lenskit.results;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.lenskit.api.Result;

import java.util.Iterator;
import java.util.Set;

public class BasicResultMap extends AbstractLong2ObjectMap<Result> implements LenskitResultMap {
    private final Long2ObjectMap<Result> delegate;

    public BasicResultMap(Long2ObjectMap<Result> map) {
        // TODO find way to avoid copy when map is already immutable
        this(map, true);
    }

    BasicResultMap(Long2ObjectMap<Result> map, boolean copy) {
        if (copy) {
            delegate = new Long2ObjectLinkedOpenHashMap<>(map);
        } else {
            delegate = map;
        }
    }

    @Override
    public Long2DoubleMap scoreMap() {
        return null;
    }

    @Override
    public Iterator<Result> iterator() {
        return Iterators.unmodifiableIterator(delegate.values().iterator());
    }

    @Override
    public ObjectSet<Entry<Result>> long2ObjectEntrySet() {
        return ObjectSets.unmodifiable(delegate.long2ObjectEntrySet());
    }

    @Override
    public Result get(long l) {
        return delegate.get(l);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Set<Result> resultSet() {
        return null;
    }

    @Override
    public double getScore(long id) {
        return 0;
    }
}
