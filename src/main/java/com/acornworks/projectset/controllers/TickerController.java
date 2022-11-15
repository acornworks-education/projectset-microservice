package com.acornworks.projectset.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.management.InvalidAttributeValueException;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.Ticker;
import com.acornworks.projectset.repositories.TickerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/ticker")
public class TickerController {
    @Autowired
    private TickerRepository tickerRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${featuretoggle.keys.strategy}")
    private String toggleKey;

    @Autowired
    private FeatureToggle featureToggle;

    @GetMapping("/list")
    public List<Ticker> getTickerList() {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            final List<Ticker> tickerList = new ArrayList<>();
            
            tickerRepository.findAll().iterator().forEachRemaining(tickerList::add);

            return tickerList;
        }
    }
    
    @PostMapping("")
    public Ticker addTicker(@RequestBody String iptJsonStr) throws Exception {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            final JsonNode inputNode = objectMapper.readTree(iptJsonStr);

            final String ticker = inputNode.get("ticker") != null ? inputNode.get("ticker").asText() : null;
            final String name = inputNode.get("name") != null ? inputNode.get("name").asText() : null;

            if (ticker != null && name != null) {
                final Ticker tickerObj = new Ticker(ticker, name);
                final Ticker savedTicker = tickerRepository.save(tickerObj);

                return savedTicker;
            } else {
                throw new InvalidAttributeValueException("KEY_TICKER_NAME_SHOULD_BE_DEFINED");
            }
        }
    }

    @DeleteMapping("")
    public Ticker removeTicker(@RequestBody String iptJsonStr) throws Exception {
        // FIXME Refactor feature toggle
        if (featureToggle.getFeature(toggleKey)) {
            throw new NotImplementedException("New price function is not implemented");
        } else {
            final JsonNode inputNode = objectMapper.readTree(iptJsonStr);

            final String ticker = inputNode.get("ticker") != null ? inputNode.get("ticker").asText() : null;

            if (ticker != null) {
                tickerRepository.deleteById(ticker);
                final Ticker deletedTicker = new Ticker(ticker, "");

                return deletedTicker;
            } else {
                throw new InvalidAttributeValueException("KEY_TICKER_SHOULD_BE_DEFINED");
            }
        }
    }

}
