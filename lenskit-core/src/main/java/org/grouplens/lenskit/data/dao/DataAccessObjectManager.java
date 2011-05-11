package org.grouplens.lenskit.data.dao;

public interface DataAccessObjectManager<T extends UserItemDataAccessObject> {

    public T open();
}
