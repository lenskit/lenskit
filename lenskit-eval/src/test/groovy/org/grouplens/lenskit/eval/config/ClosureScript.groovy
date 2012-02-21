package org.grouplens.lenskit.eval.config

/**
 * Eval config script that invokes a closure rather than running a script.
 * @author Michael Ekstrand
 */
class ClosureScript extends EvalConfigScript {
    Closure closure

    ClosureScript(EvalConfigEngine engine, Closure cl) {
        super(engine)
        closure = cl
    }

    @Override
    def run() {
        closure.setDelegate(this)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        return closure.call()
    }
}
