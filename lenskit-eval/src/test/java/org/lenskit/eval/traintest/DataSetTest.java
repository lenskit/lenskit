package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.lenskit.data.entities.CommonTypes.*;

/**
 * Created by kiranthapa on 2/9/17.
 */
public class DataSetTest {
    private ObjectReader reader = new ObjectMapper().reader();

    @Test
    public void testEntityTypeArray() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"items.json\",\n" +
                "\"name\": \"movie\", \n" +
//                "\"base_uri\": \" \", \n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"},\n" +
                "\"entity_types\": [\"user\", \"item\"]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(USER, ITEM));
    }

    @Test
    public void testEntityTypeString() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"items.json\",\n" +
                "\"name\": \"movie\", \n" +
//                "\"base_uri\": \" \", \n" +
                "\"datasets\": [{\n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"},\n" +
                "\"entity_types\":  \"item\"\n" +
                "}]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(ITEM));
    }

    @Test
    public void testEntityTypeNone() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"items.json\",\n" +
                "\"name\": \"movie\", \n" +
//                "\"base_uri\": \" \", \n" +
                "\"datasets\": [{\n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"}\n" +
                "}]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(RATING));
    }

}