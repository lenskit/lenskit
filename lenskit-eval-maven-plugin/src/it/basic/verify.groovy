import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

File file = new File(basedir, "out.txt")

assertThat("output file existence",
           file.isFile(), equalTo(true));
assertThat("output file text",
           file.getText(), equalTo("test ran!"))