package com.example.cardprocessingservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CBURate {
    @JsonProperty("Ccy")
    private String ccy;

    @JsonProperty("Rate")
    private BigDecimal rate;
}
