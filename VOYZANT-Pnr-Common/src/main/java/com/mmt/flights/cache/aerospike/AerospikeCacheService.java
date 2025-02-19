package com.mmt.flights.cache.aerospike;


import com.mmt.flights.cache.api.Cache;
import com.mmt.flights.cache.core.AerospikeConfig;
import com.mmt.flights.cache.core.CacheConfig;
import com.mmt.flights.cache.core.CacheManager;
import com.mmt.flights.cache.core.CacheType;
import com.mmt.flights.cache.exception.CacheException;
import com.mmt.flights.compression.core.CompressionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class AerospikeCacheService {

    private Cache cache;
    private Cache cache2;

    @Autowired
    private AerospikeBaseConfig aerospikeProperties;

    @PostConstruct
    public void init() {
        this.cache = CacheManager.getInstance(createAerospikeConfig(true),  CacheType.AEROSPIKE);
        this.cache2 = CacheManager.getInstance(createAerospikeConfig(false),  CacheType.AEROSPIKE);
    }

    private CacheConfig createAerospikeConfig(boolean compression) {
        AerospikeConfig aerospikeConfig = new AerospikeConfig();
        aerospikeConfig.setHostAndPort(aerospikeProperties.getHostAndPort());
        aerospikeConfig.setNamespace(aerospikeProperties.getNamespace());
        aerospikeConfig.setSet(aerospikeProperties.getSet());
        aerospikeConfig.setSecretName(aerospikeProperties.getSecretName());
        aerospikeConfig.setFallbackEnabled(aerospikeProperties.isFallbackEnabled());
        aerospikeConfig.setReadPolicySleepBetweenRetries(aerospikeProperties.getReadPolicySleepBetweenRetries());
        aerospikeConfig.setReadPolicySocketTimeout(aerospikeProperties.getReadPolicySocketTimeout());
        aerospikeConfig.setReadPolicyTotalTimeout(aerospikeProperties.getReadPolicyTotalTimeout());

        aerospikeConfig.setWritePolicySleepBetweenRetries(aerospikeProperties.getWritePolicySleepBetweenRetries());
        aerospikeConfig.setWritePolicySocketTimeout(aerospikeProperties.getWritePolicySocketTimeout());
        aerospikeConfig.setWritePolicyTotalTimeout(aerospikeProperties.getWritePolicyTotalTimeout());

        if(compression) {
            aerospikeConfig.setEnableCompression(true);
            aerospikeConfig.setType(CompressionType.LZ4);
        }
        return aerospikeConfig;
    }

    public void put(String key, String value, String binName, int expiry) throws CacheException {
        Map<String, String> bins = new HashMap<>();
        bins.put(binName, value);
        cache.multiPUT(key, bins, expiry);
    }

    public String get(String key, String binName) throws CacheException {
        return cache.get(key, binName);
    }

    public void putData(String key, String jsonData, String binName, int expiry) {
        Map<String, byte[]> store = new HashMap<>();
        store.put(binName, jsonData.getBytes());
        cache.multiCREATEorUPDATE(key, store, expiry);
    }

    public void putDataWithoutCompression(String key, String jsonData, String binName, int expiry) {
        Map<String, byte[]> store = new HashMap<>();
        store.put(binName, jsonData.getBytes());
        cache2.multiCREATEorUPDATE(key, store, expiry);
    }

    public void put(String key, String value, String binName) throws CacheException {
        put(key, value, binName, aerospikeProperties.getExpiryInSeconds());
    }

    public void put(String key, String counter,int expiry) throws CacheException {
       cache.put(key,counter,expiry);

    }
    public String get(String key) throws CacheException {
        return cache.get(key);

    }
    public Long incrementNotificationCount(String key)  {
        return cache.incrementAndGet(key);
    }
    public Long getCount(String key) throws CacheException {
        Object countData = cache.getRawData(key, "bin1");
        if(countData!=null){
            return (Long) countData;
        }
        return 0L;
    }
}
