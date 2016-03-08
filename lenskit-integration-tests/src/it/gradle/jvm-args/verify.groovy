import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

logger.info('testing for the configured property')
assertThat(System.getProperties(), hasEntry('test.prop', 'test-value'))
