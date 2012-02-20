package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Evaluation
import org.slf4j.LoggerFactory

/**
 * Base class for evaluator configuration scripts. It contains the metaclass
 * machinery to set up evaluation tasks.
 * @author Michael Ekstrand
 * @since 0.10
 */
abstract class EvalConfigScript extends Script {
    private List<Evaluation> evaluations = new ArrayList<Evaluation>()
    private Map<String,BuilderFactory> factories;
    protected final def logger = LoggerFactory.getLogger(getClass())

    EvalConfigScript() {
        def loader = ServiceLoader.load(BuilderFactory)
        factories = new HashMap()
        for (f in loader) {
            factories.put(f.name, f)
        }
    }

    def methodMissing(String name, args) {
        logger.debug("searching for eval task {}", name)
        Closure cl = null;
        if (args.length >= 0 && args[args.length - 1] instanceof Closure) {
            cl = (Closure) args[args.length - 1]
        }
        def svc = factories.get(name)
        if (svc == null) throw new MissingMethodException(name, this.class, args)

        logger.info("configuring evaluation with provider {}", svc.name)
        def builder = svc.newBuilder()
        def ret = null
        if (cl != null) {
            BuilderDelegate delegate = new BuilderDelegate(builder)
            ret = delegate.apply(cl)
        }
        evaluations.add(builder.build())
        return ret
    }

    List<Evaluation> getEvaluations() {
        return evaluations
    }
}
