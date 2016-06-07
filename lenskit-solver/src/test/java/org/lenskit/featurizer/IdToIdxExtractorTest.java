package org.lenskit.featurizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class IdToIdxExtractorTest {

    @Test
    public void testExtractor() {
        Entity entity = new Entity();
        entity.addCatAttr("haha", "haha");
        entity.setNumAttr("haha", 1.0);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String strEntity = mapper.writeValueAsString(entity);
            int x = 1;
        } catch (JsonProcessingException e) {

        }
    }
}
