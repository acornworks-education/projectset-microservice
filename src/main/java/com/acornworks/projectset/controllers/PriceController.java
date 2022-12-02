package com.acornworks.projectset.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.SpotData;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.processors.HistoricalDataProcessor;
import com.opencsv.exceptions.CsvException;

@RestController
@RequestMapping("/price")
public class PriceController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HistoricalDataProcessor processor;

    @Value("${featuretoggle.keys.price}")
    private String toggleKey;

    @Value("${microservice.price}")
    private String priceUrl;

    @Autowired
    private FeatureToggle featureToggle;

    @GetMapping(value = "/spot/{symbol}", produces = "application/json")
    public SpotData getSpotPrice(@PathVariable("symbol") String symbol) {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            final String callUrl = String.format("%s/spot/%s", priceUrl, symbol);
            logger.info("Call URL for getSpotPrice: {}", callUrl);

            final SpotData responseData = restTemplate.getForObject(callUrl, SpotData.class);

            return responseData;
        } else {
            return processor.getPrice(symbol);
        }
    }

    @GetMapping(value = "/historical/{symbol}", produces = "application/json")
    public List<StockPrice> getHistoricalPrices(@PathVariable("symbol") String symbol) throws IOException, CsvException, ParseException {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            final String callUrl = String.format("%s/historical/%s", priceUrl, symbol);
            logger.info("Call URL for getHistoricalPrices: {}", callUrl);

            ResponseEntity<StockPrice[]> response = restTemplate.getForEntity(callUrl, StockPrice[].class);

            return Arrays.asList(response.getBody());
        } else {
            return processor.getHisoricalPrice(symbol);
        }
    }    
}
