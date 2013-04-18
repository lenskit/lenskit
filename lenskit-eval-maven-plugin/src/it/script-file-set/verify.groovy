import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

File dir = new File("${basedir}/target/analysis")
assertThat(new File(dir, "foo.txt").getText(),
           equalTo("hello, world"))
assertThat(new File(dir, "bar.txt").getText(),
           equalTo("good night moon"))