package com.acornworks.projectset.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.InvalidAttributeValueException;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.Ticker;
import com.acornworks.projectset.repositories.TickerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/ticker")
public class TickerController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TickerRepository tickerRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${featuretoggle.keys.ticker}")
    private String toggleKey;

    @Autowired
    private FeatureToggle featureToggle;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservice.ticker}")
    private String tickerUrl;

    @GetMapping("/list")
    public List<Ticker> getTickerList() {
        if (featureToggle.getFeature(toggleKey)) {
            final String callUrl = String.format("%s/list", tickerUrl);
            logger.info("getTickerList Call URL: {}", callUrl);
            ResponseEntity<Ticker[]> response = restTemplate.getForEntity(callUrl, Ticker[].class);

            return Arrays.asList(response.getBody());
        } else {
            final List<Ticker> tickerList = new ArrayList<>();
            
            tickerRepository.findAll().iterator().forEachRemaining(tickerList::add);

            return tickerList;
        }
    }
    
    @PostMapping("")
    public Ticker addTicker(@RequestBody String iptJsonStr) throws Exception {
        // FIXME Refactor feature toggle
        final JsonNode inputNode = objectMapper.readTree(iptJsonStr);

        final String ticker = inputNode.get("ticker") != null ? inputNode.get("ticker").asText() : null;
        final String name = inputNode.get("name") != null ? inputNode.get("name").asText() : null;

        if (ticker != null && name != null) {
            final Ticker tickerObj = new Ticker(ticker, name);

            if (featureToggle.getFeature(toggleKey)) {
                HttpEntity<Ticker> requestEntity = new HttpEntity<>(tickerObj);

                logger.info("addTicker Call URL: {}", tickerUrl);
                ResponseEntity<Ticker> response = restTemplate.exchange(tickerUrl, HttpMethod.POST, requestEntity, Ticker.class);

                return response.getBody();
            } else {
                final Ticker savedTicker = tickerRepository.save(tickerObj);

                return savedTicker;    
            }
        } else {
            throw new InvalidAttributeValueException("KEY_TICKER_NAME_SHOULD_BE_DEFINED");
        }
    }

    @DeleteMapping("")
    public Ticker removeTicker(@RequestBody String iptJsonStr) throws Exception {
        final JsonNode inputNode = objectMapper.readTree(iptJsonStr);

        final String ticker = inputNode.get("ticker") != null ? inputNode.get("ticker").asText() : null;

        if (ticker != null) {
            if (featureToggle.getFeature(toggleKey)) {                
                final Ticker tickerObj = new Ticker(ticker, "");
                HttpEntity<Ticker> requestEntity = new HttpEntity<>(tickerObj);

                logger.info("removeTicker Call URL: {}", tickerUrl);
                ResponseEntity<Ticker> response = restTemplate.exchange(tickerUrl, HttpMethod.DELETE, requestEntity, Ticker.class);

                return response.getBody();
            } else {
                tickerRepository.deleteById(ticker);
                final Ticker deletedTicker = new Ticker(ticker, "");
                return deletedTicker;
            }
        } else {
            throw new InvalidAttributeValueException("KEY_TICKER_SHOULD_BE_DEFINED");
        }
    }

}
