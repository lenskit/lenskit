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
        def method = ConfigHelpers.findBuilderMethod(engine, name, args)
        if (method != null) {
            def obj = method()
            // FIXME Should we really add this evaluation? Or how do we want to handle that?
            evaluations.add(obj)
            return obj
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    List<Evaluation> getEvaluations() {
        return evaluations
    }
}
