import org.lenskit.api.ItemScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.data.entities.EntityType
import org.lenskit.data.ratings.InteractionEntityType

bind ItemScorer to PopularityRankItemScorer
set InteractionEntityType to EntityType.forName("bookmark")
