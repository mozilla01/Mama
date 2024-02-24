package org.mamallc.crawler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Page {
        public Map<String, Integer> textSet = new HashMap<>();
        Date lastVisited = new Date();

        public void insertPage(Page pg) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                        MongoDatabase mamaDB= mongoClient.getDatabase("mama");
                        System.out.println(mamaDB.listCollectionNames());
//                        MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("grades");
                }
        }
}
