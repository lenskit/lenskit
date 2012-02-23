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
    protected final def logger = LoggerFactory.getLogger(getClass())
    private EvalConfigEngine engine

    EvalConfigScript() {
        engine = null
    }

    EvalConfigScript(EvalConfigEngine eng) {
        engine = eng
    }

    void setEngine(EvalConfigEngine eng) {
        engine = eng
    }

    EvalConfigEngine getEngine() {
        return engine
    }

    def methodMissing(String name, args) {
        logger.debug("searching for eval task {}", name)
        Closure cl = null
        if (args.length >= 0 && args[args.length - 1] instanceof Closure) {
            cl = (Closure) args[args.length - 1]
        }
        String val = null
        if (args.length >= 0 && !(args[0] instanceof Closure)) {
            val = args[0].toString()
        }

        def svc = engine.getBuilderFactory(name)
        if (svc == null) throw new MissingMethodException(name, this.class, args)

        def obj = ConfigHelpers.invokeBuilderFromFactory(svc, val, cl)
        // FIXME Should we really add this evaluation? Or how do we want to handle that?
        evaluations.add(obj)
        return obj
    }

    List<Evaluation> getEvaluations() {
        return evaluations
    }
}
