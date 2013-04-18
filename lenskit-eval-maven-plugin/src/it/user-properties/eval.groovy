import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

// test model prop
assertThat(config["model.prop.foo"],
           equalTo("ZELGO MER"))
// test short prop
assertThat(config.shortProp,
           equalTo("KIRJE"))
// test user prop
assertThat(config["user.prop.uncontested"],
           equalTo("HACKEM MUCHE"))
// test overridden prop
assertThat(config["user.prop.overrides"],
           equalTo("FOOBIE BLETCH"))