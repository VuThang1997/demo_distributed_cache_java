package com.example.demodistributedcache.model;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class GlobalCache {

    private final Map<Integer, CompletableFuture<KeyValue>> cache = new ConcurrentHashMap<>();


    public KeyValue getValueFromCache(Integer key) {
        CompletableFuture<KeyValue> future = cache.get(key);
        if (future == null) {
            future = CompletableFuture
                    .supplyAsync(() -> computeValueFromInternet(key))
                    .thenApply(strValue -> new KeyValue(key, strValue));
            cache.put(key, future);
        }

        return future.join();
    }

    private String computeValueFromInternet(Integer key) {
        System.out.println("-------------- Sleep 3s to get value for key " + key + ", time = " + LocalDateTime.now());
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        String value = key + "" + random.nextInt(1000);

        System.out.println("-------------- Finish get value for key " + key + ", time = " + LocalDateTime.now());
        return value;
    }

    public List<KeyValue> getValuesFromCache(List<Integer> keys) {
        List<CompletableFuture<KeyValue>> futureList = new ArrayList<>(keys.size());

        // check with progressingKeys
        for (Integer key : keys) {
            // if have key then get CompletableFuture
            CompletableFuture<KeyValue> future = cache.get(key);

            // if not have key then generate new CompletableFuture
            if (future == null) {
                future = CompletableFuture
                        .supplyAsync(() -> computeValueFromInternet(key))
                        .thenApply(strValue -> new KeyValue(key, strValue));
                cache.put(key, future);
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
        CompletableFuture<KeyValue> future = cache.remove(key);
        if (future != null) {
            return future.join();
        }
        return new KeyValue(key, "Not exist this key in cache");
    }
}
