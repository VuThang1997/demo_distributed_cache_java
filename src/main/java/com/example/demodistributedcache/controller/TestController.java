package com.example.demodistributedcache.controller;

import com.example.demodistributedcache.model.GlobalCache;
import com.example.demodistributedcache.model.KeyValue;
import com.example.demodistributedcache.model.MultipleKeyRequest;
import com.example.demodistributedcache.model.MultipleKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class TestController {

    @Autowired
    private GlobalCache globalCache;

    @GetMapping("/get/{key}")
    public KeyValue getFromKey(@PathVariable Integer key) {
        return globalCache.getValueFromCache(key);
    }

    @PostMapping("/get/keys")
    public MultipleKeyResponse getFromListKey(@RequestBody MultipleKeyRequest request) {
        List<Integer> keys = request.getKeys();
        List<KeyValue> keyValueList = globalCache.getValuesFromCache(keys);
        return new MultipleKeyResponse(keyValueList);
    }

    @DeleteMapping("/{key}")
    public KeyValue invalidateKey(@PathVariable Integer key) {
        return globalCache.removeKey(key);
    }
}
