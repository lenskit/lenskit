package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Evaluation
import org.grouplens.lenskit.eval.EvalProvider
import org.slf4j.LoggerFactory

/**
 * @author Michael Ekstrand
 */
abstract class EvalConfigScript extends Script {
    private List<Evaluation> evaluations = new ArrayList<Evaluation>()
    private ServiceLoader<EvalProvider> loader = ServiceLoader.load(EvalProvider)
    protected final def logger = LoggerFactory.getLogger(getClass())

    def methodMissing(String name, args) {
        logger.debug("searching for eval task {}", name)
        Closure cl = null;
        if (args.length >= 0 && args[args.length - 1] instanceof Closure) {
            cl = (Closure) args[args.length - 1]
        }
        for (svc in loader) {
            if (svc.name == name.capitalize()) {
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
        }
        // if we got this far, no such configuration
        throw new RuntimeException("no provider available for eval " + name);
    }

    List<Evaluation> getEvaluations() {
        return evaluations
    }
}
