package org.lenskit.slim;

import javax.inject.Provider;

/**
 * Created by tmc on 3/12/17.
 */
public class SLIMModelProvider implements Provider<SLIMModel>{


    @Override
    public SLIMModel get() {
        return new SLIMModel();
    }
}
