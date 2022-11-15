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
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.domains.StrategyName;
import com.acornworks.projectset.domains.TradingResult;
import com.acornworks.projectset.processors.HistoricalDataProcessor;
import com.acornworks.projectset.processors.StrategyProcessor;
import com.opencsv.exceptions.CsvException;

@RestController
@RequestMapping("/strategy")
public class StrategyController {
    @Autowired
    private HistoricalDataProcessor historicalDataProcessor;

    @Autowired
    private StrategyProcessor strategyProcessor;

    @Value("${featuretoggle.keys.strategy}")
    private String toggleKey;

    @Autowired
    private FeatureToggle featureToggle;

    @GetMapping(value = "/{symbol}/{strategy}", produces = "application/json")
    public TradingResult getAnalysis(@PathVariable("symbol") String symbol, @PathVariable("strategy") String strategyStr) throws IOException, CsvException, ParseException {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            final StrategyName strategy = StrategyName.valueOf(strategyStr);
            final List<StockPrice> stockPrices = historicalDataProcessor.getHisoricalPrice(symbol);

            return strategyProcessor.getAnalysisResult(stockPrices, strategy);
        }
    }

    
}
