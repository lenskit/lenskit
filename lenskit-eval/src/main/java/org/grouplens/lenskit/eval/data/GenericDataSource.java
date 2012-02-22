package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;

/**
 * @author Michael Ekstrand
 */
public class GenericDataSource implements DataSource {
    private String name;
    private DAOFactory daoFactory;
    private PreferenceDomain domain;

    public GenericDataSource(String name, DAOFactory factory) {
        this(name, factory, null);
    }

    public GenericDataSource(String name, DAOFactory factory, PreferenceDomain dom) {
        this.name = name;
        daoFactory = factory;
        domain = dom;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public DAOFactory getDAOFactory() {
        return daoFactory;
    }

    @Override
    public long lastUpdated(PreparationContext context) {
        return 0;
    }

    @Override
    public void prepare(PreparationContext context) throws PreparationException {
        /* no-op */
    }
}
