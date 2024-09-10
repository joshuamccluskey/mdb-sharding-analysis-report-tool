package com.example.mongodbsharding;

import com.mongodb.client.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MongoDBShardingService {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    public ShardingStatus getShardingStatus() {
        ShardingStatus status = new ShardingStatus();

        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoDatabase adminDb = client.getDatabase("admin");
            MongoDatabase configDb = client.getDatabase("config");

            // Get list of shards
            Document listShardsResult = adminDb.runCommand(new Document("listShards", 1));
            List<Document> shards = (List<Document>) listShardsResult.get("shards");
            status.setShards(new ArrayList<>());
            for (Document shard : shards) {
                ShardingStatus.Shard shardInfo = new ShardingStatus.Shard();
                shardInfo.setId(shard.getString("_id"));
                shardInfo.setHost(shard.getString("host"));
                status.getShards().add(shardInfo);
            }

            // Get databases and their sharding status
            Document listDatabasesResult = adminDb.runCommand(new Document("listDatabases", 1));
            List<Document> databases = (List<Document>) listDatabasesResult.get("databases");
            status.setDatabases(new ArrayList<>());
            for (Document db : databases) {
                String dbName = db.getString("name");
                MongoDatabase dbInfo = client.getDatabase(dbName);
                ShardingStatus.Database dbShardingInfo = new ShardingStatus.Database();
                dbShardingInfo.setName(dbName);
                dbShardingInfo.setShardedCollections(new ArrayList<>());

                // Get sharded collections info from config.collections
                MongoCollection<Document> collections = configDb.getCollection("collections");
                FindIterable<Document> collectionDocs = collections.find(new Document("_id", new Document("$regex", "^" + dbName + "\\.")));
                for (Document coll : collectionDocs) {
                    String collName = coll.getString("_id").split("\\.", 2)[1];
                    Document collStats = dbInfo.runCommand(new Document("collStats", collName));

                    ShardingStatus.ShardedCollection shardedColl = new ShardingStatus.ShardedCollection();
                    shardedColl.setName(collName);
                    shardedColl.setShardKey(coll.get("key"));
                    shardedColl.setChunkCount(collStats.getInteger("nchunks", 0));
                    shardedColl.setShardDistribution(collStats.get("shards"));

                    dbShardingInfo.getShardedCollections().add(shardedColl);
                }

                status.getDatabases().add(dbShardingInfo);
            }
        }

        return status;
    }
}