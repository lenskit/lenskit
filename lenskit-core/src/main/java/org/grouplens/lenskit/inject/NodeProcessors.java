package org.grouplens.lenskit.inject;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NodeProcessors {
    /**
     * Create a node processor that will instantiate nodes.  It will return nodes whose satisfactions
     * have been replaced with instance satisfactions containing the instance.
     *
     * @return The node processor.
     */
    public static NodeProcessor instantiate() {
        return instantiate(NodeInstantiator.create());
    }

    /**
     * Create a node processor that will instantiate nodes.  It will return nodes whose satisfactions
     * have been replaced with instance satisfactions containing the instance.
     *
     * @param inst The node instantiator to use when instantiating nodes.
     * @return The node processor.
     */
    public static NodeProcessor instantiate(NodeInstantiator inst) {
        return new InstantiatingNodeProcessor(inst);
    }

    /**
     * Create a node processor that will simulate instantiating nodes.
     * @return The node processor.
     */
    public static NodeProcessor simulateInstantiation() {
        return new SimulationNodeProcessor();
    }
}
