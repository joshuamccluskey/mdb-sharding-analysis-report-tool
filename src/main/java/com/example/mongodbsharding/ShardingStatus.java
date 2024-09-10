package com.example.mongodbsharding;

import java.util.List;

public class ShardingStatus {
    private List<Shard> shards;
    private String configServers;
    private List<Database> databases;

    // Getters and setters
    public List<Shard> getShards() {
        return shards;
    }

    public void setShards(List<Shard> shards) {
        this.shards = shards;
    }

    public String getConfigServers() {
        return configServers;
    }

    public void setConfigServers(String configServers) {
        this.configServers = configServers;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public static class Shard {
        private String id;
        private String host;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    public static class Database {
        private String name;
        private String primary;
        private List<ShardedCollection> shardedCollections;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrimary() {
            return primary;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }

        public List<ShardedCollection> getShardedCollections() {
            return shardedCollections;
        }

        public void setShardedCollections(List<ShardedCollection> shardedCollections) {
            this.shardedCollections = shardedCollections;
        }
    }

    public static class ShardedCollection {
        private String name;
        private Object shardKey;
        private int chunkCount;
        private Object shardDistribution;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getShardKey() {
            return shardKey;
        }

        public void setShardKey(Object shardKey) {
            this.shardKey = shardKey;
        }

        public int getChunkCount() {
            return chunkCount;
        }

        public void setChunkCount(int chunkCount) {
            this.chunkCount = chunkCount;
        }

        public Object getShardDistribution() {
            return shardDistribution;
        }

        public void setShardDistribution(Object shardDistribution) {
            this.shardDistribution = shardDistribution;
        }
    }
}