package com.example.cardprocessingservice.service;

import com.example.cardprocessingservice.model.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final Logger log = LogManager.getLogger(CurrencyService.class);

    private final RestTemplate restTemplate;

    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        log.info("Fetching exchange rate from {} to {}", fromCurrency, toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            log.debug("Currencies are the same: {} -> {}, returning 1", fromCurrency, toCurrency);
            return BigDecimal.ONE;
        }

        if ((fromCurrency == Currency.USD && toCurrency == Currency.UZS) ||
                (fromCurrency == Currency.UZS && toCurrency == Currency.USD)) {

            String url = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/";
            try {
                ResponseEntity<CBURate[]> response = restTemplate.getForEntity(url, CBURate[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    for (CBURate rate : response.getBody()) {
                        if (rate.getCcy().equalsIgnoreCase("USD")) {
                            BigDecimal usdRate = rate.getRate(); // USD -> UZS
                            log.info("USD rate fetched from CBU: {}", usdRate);

                            BigDecimal result = (fromCurrency == Currency.USD)
                                    ? usdRate.setScale(2, RoundingMode.HALF_UP)
                                    : BigDecimal.ONE.divide(usdRate, 6, RoundingMode.HALF_UP);

                            log.info("Calculated exchange rate: {} -> {} = {}", fromCurrency, toCurrency, result);
                            return result;
                        }
                    }
                }
                log.warn("USD rate not found in CBU response");
                throw new RuntimeException("USD rate not found in CBU response");

            } catch (Exception e) {
                log.error("Failed to fetch exchange rate from CBU: {}", e.getMessage(), e);
                throw new RuntimeException("Error fetching exchange rate from CBU", e);
            }
        }

        log.warn("Unsupported exchange pair: {} -> {}", fromCurrency, toCurrency);
        throw new RuntimeException("Only USD <-> UZS exchange supported");
    }
}
