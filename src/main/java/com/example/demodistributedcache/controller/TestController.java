package com.example.demodistributedcache.controller;

import com.example.demodistributedcache.model.GlobalCache;
import com.example.demodistributedcache.model.KeyValue;
import com.example.demodistributedcache.model.MultipleKeyRequest;
import com.example.demodistributedcache.model.MultipleKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class TestController {

    @Autowired
    private GlobalCache globalCache;

    @GetMapping("/get/{key}")
    public KeyValue getFromKey(@PathVariable Integer key) {
        // temporary not handle multi-thread
        String value = globalCache.getValueFromCache(key);
        if (value == null) {
            value = globalCache.getValueFromInternet(key);
        }
        return new KeyValue(key, value);
    }

    @PostMapping("/get/keys")
    public MultipleKeyResponse getFromListKey(@RequestBody MultipleKeyRequest request) {
        List<Integer> keys = request.getKeys();
        List<Integer> keyNotHaveValueList = new ArrayList<>(keys.size());
        List<KeyValue> keyValueList = new ArrayList<>(keys.size());

        for (Integer key : keys) {
            String value = globalCache.getValueFromCache(key);
            if (value != null) {
                keyValueList.add(new KeyValue(key, value));
            } else {
                keyNotHaveValueList.add(key);
            }
        }

        List<KeyValue> keyValueList2 = globalCache.getValuesFromInternet(keyNotHaveValueList);
        keyValueList.addAll(keyValueList2);

        return new MultipleKeyResponse(keyValueList);
    }

    @DeleteMapping("/{key}")
    public KeyValue invalidateKey(@PathVariable Integer key) {
        return globalCache.removeKey(key);
    }
}
