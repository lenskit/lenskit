package org.grouplens.lenskit.data.sql;

/**
 * Key type for caching JDBC queries.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class QueryKey {
    public static enum Type {
        USER, ITEM
    }

    private final Type queryType;
    private final long queryId;

    QueryKey(Type type, long id) {
        queryType = type;
        queryId = id;
    }

    public Type getQueryType() {
        return queryType;
    }

    public long getQueryId() {
        return queryId;
    }

    public static QueryKey user(long id) {
        return new QueryKey(Type.USER, id);
    }

    public static QueryKey item(long id) {
        return new QueryKey(Type.ITEM, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryKey queryKey = (QueryKey) o;

        if (queryId != queryKey.queryId) return false;
        if (queryType != queryKey.queryType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryType != null ? queryType.hashCode() : 0;
        result = 31 * result + (int) (queryId ^ (queryId >>> 32));
        return result;
    }
}
