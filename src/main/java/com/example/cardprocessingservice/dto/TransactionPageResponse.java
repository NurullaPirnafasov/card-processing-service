package com.example.cardprocessingservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransactionPageResponse {
    private int page;
    private int size;
    private int total_pages;
    private long total_items;
    private List<Object> content;
}
