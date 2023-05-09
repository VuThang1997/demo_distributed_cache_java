package com.example.demodistributedcache.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class GlobalCache {

    private final Map<Integer, String> cache = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<KeyValue>> progressingKeys = new ConcurrentHashMap<>();


    public String getValueFromCache(Integer key) {
        return cache.get(key);
    }

    public String getValueFromInternet(Integer key) {
        CompletableFuture<KeyValue> future = progressingKeys.get(key);
        if (future == null) {
            // need to test correctness of these lines
            future = CompletableFuture
                    .supplyAsync(() -> computeValueFromInternet(key))
                    .thenApply(strValue -> new KeyValue(key, strValue))
                    .thenApply(keyValue -> {
                        cache.put(key, keyValue.getValue());
                        progressingKeys.remove(key);
                        return keyValue;
                    });
            progressingKeys.put(key, future);
        }

        return future.join().getValue();
    }

    private String computeValueFromInternet(Integer key) {
        System.out.println("-------------- Sleep 3s to get value for key " + key );
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        return key + "" + random.nextInt(1000);
    }

    public List<KeyValue> getValuesFromInternet(List<Integer> keys) {
        List<CompletableFuture<KeyValue>> futureList = new ArrayList<>(keys.size());

        // check with progressingKeys
        for (Integer key : keys) {
            // if have key then get CompletableFuture
            CompletableFuture<KeyValue> future = progressingKeys.get(key);

            // if not have key then generate new CompletableFuture
            if (future == null) {
                // need to test correctness of these lines
                future = CompletableFuture
                        .supplyAsync(() -> computeValueFromInternet(key))
                        .thenApply(strValue -> new KeyValue(key, strValue))
                        .thenApply(keyValue -> {
                            cache.put(key, keyValue.getValue());
                            progressingKeys.remove(key);
                            return keyValue;
                        });
                progressingKeys.put(key, future);
            }

            futureList.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        return allFutures.thenApply(v -> futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
                .join();
    }

    public KeyValue removeKey(Integer key) {
        String value = cache.remove(key);
        return new KeyValue(key, value);
    }
}
