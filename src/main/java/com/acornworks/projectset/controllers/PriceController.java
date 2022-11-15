package com.acornworks.projectset.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.SpotData;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.processors.HistoricalDataProcessor;
import com.opencsv.exceptions.CsvException;

@RestController
@RequestMapping("/price")
public class PriceController {
    @Autowired
    private HistoricalDataProcessor processor;

    @Value("${featuretoggle.keys.price}")
    private String toggleKey;

    @Autowired
    private FeatureToggle featureToggle;

    @GetMapping(value = "/spot/{symbol}", produces = "application/json")
    public SpotData getSpotPrice(@PathVariable("symbol") String symbol) {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            return processor.getPrice(symbol);
        }
    }

    @GetMapping(value = "/historical/{symbol}", produces = "application/json")
    public List<StockPrice> getHistoricalPrices(@PathVariable("symbol") String symbol) throws IOException, CsvException, ParseException {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            return processor.getHisoricalPrice(symbol);
        }
    }    
}
