package com.example.demodistributedcache.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleKeyResponse {
    private List<KeyValue> keyValueList;
}
