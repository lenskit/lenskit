import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO
import org.lenskit.data.ratings.Ratings

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

def dao = BinaryRatingDAO.open(new File('build/header.pack'))
assertThat(dao.streamEvents(), contains(Ratings.make(42, 3, 3.5),
                                        Ratings.make(1024, 51, 5)))
