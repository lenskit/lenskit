package org.grouplens.lenskit.data.dao;

/**
 * Factory to create new DAOs.  This is used when something needs to be able to
 * create DAOs at will.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DAOFactory<T> {
    /**
     * Create a new DAO.
     * @return A new DAO.  The caller is responsible to close it.
     */
    T create();
}