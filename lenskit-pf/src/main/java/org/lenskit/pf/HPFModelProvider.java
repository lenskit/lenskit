package org.lenskit.pf;


import javax.inject.Inject;
import javax.inject.Provider;

public class HPFModelProvider implements Provider<HPFModel> {
    private final RandomInitializationStrategy randomInitials;
    private final RandomDataSplitStrategyProvider ratings;


    @Inject
    public HPFModelProvider(RandomInitializationStrategy rndInitls,
                            RandomDataSplitStrategyProvider rndRatings) {
        randomInitials = rndInitls;
        ratings = rndRatings;

    }

    @Override
    public HPFModel get() {

        return null;
    }
}
