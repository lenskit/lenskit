import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

File file = new File("${basedir}/target/analysis", "out.txt")
File dataPath = new File("${basedir}/data")

assertThat("output file existence", file.isFile(), equalTo(true))
assertThat("output file text",
           file.getText(),
           equalTo(dataPath.getAbsolutePath()))