/*
 * We use a Groovy-based logback config so it overrides the default one in the eval code
 * without warnings. Since Eval pulls in Groovy, this is just fine.
 */

import static ch.qos.logback.classic.Level.*

import org.grouplens.lenskit.eval.maven.MavenLogAppender
import ch.qos.logback.classic.PatternLayout

appender("Maven", MavenLogAppender) {
    layout(PatternLayout) {
        pattern = "%logger{24} - %msg"
    }
}
logger("org.grouplens.grapht", WARN)
root(DEBUG, ["Maven"])
