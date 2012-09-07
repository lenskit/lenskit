package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Command
import org.grouplens.lenskit.eval.AlgorithmInstanceCommand
import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * Command runner for algorithm instance commands. This runner runs the command
 * with {@link AlgorithmInstanceCommandDelegate} as the delegate, and with
 * {@link ContextExtensions} as an active category.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
class AlgorithmInstanceCommandRunner extends DefaultCommandRunner {
    AlgorithmInstanceCommandRunner(EvalConfigEngine eng) {
        super(eng)
    }

    @Override
    protected def getDelegate(Command cmd) {
        new AlgorithmInstanceCommandDelegate((AlgorithmInstanceCommand) cmd)
    }

    @Override
    protected def invokeClosure(Closure closure) {
        GroovyCategorySupport.use(ContextExtensions, closure);
    }
}
