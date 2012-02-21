package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Evaluation
import org.slf4j.LoggerFactory
import com.google.common.base.Preconditions

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
    private Map<String,BuilderFactory> factoryCache

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

    Map<String,BuilderFactory> getFactories() {
        if (factoryCache == null) {
            Preconditions.checkState(engine != null, "no engine configured")
            factoryCache = engine.loadFactories()
        }
        return factoryCache
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

        def svc = factories.get(name)
        if (svc == null) throw new MissingMethodException(name, this.class, args)

        logger.info("configuring evaluation with provider {}", svc.name)
        def builder = svc.newBuilder(val)
        if (cl != null) {
            BuilderDelegate delegate = new BuilderDelegate(builder)
            delegate.apply(cl)
        }
        def obj = builder.build()
        // FIXME Should we really add this evaluation? Or how do we want to handle that?
        evaluations.add(obj)
        return obj
    }

    List<Evaluation> getEvaluations() {
        return evaluations
    }
}
