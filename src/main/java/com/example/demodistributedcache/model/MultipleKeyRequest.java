package com.example.demodistributedcache.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MultipleKeyRequest {
    private List<Integer> keys;
}
