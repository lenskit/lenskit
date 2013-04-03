import org.grouplens.lenskit.GlobalItemRecommender
import org.grouplens.lenskit.GlobalItemScorer
import org.grouplens.lenskit.ItemRecommender
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.knn.item.ItemItemGlobalRecommender
import org.grouplens.lenskit.knn.item.ItemItemGlobalScorer
import org.grouplens.lenskit.knn.item.ItemItemRecommender
import org.grouplens.lenskit.knn.item.ItemItemScorer

dumpGraph {
    output "${config.analysisDir}/item-item.dot"
    algorithm {
        bind ItemScorer to ItemItemScorer
        bind ItemRecommender to ItemItemRecommender
        bind GlobalItemScorer to ItemItemGlobalScorer
        bind GlobalItemRecommender to ItemItemGlobalRecommender
    }
}