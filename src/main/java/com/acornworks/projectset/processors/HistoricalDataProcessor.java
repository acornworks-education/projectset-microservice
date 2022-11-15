package com.acornworks.projectset.processors;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.acornworks.projectset.domains.SpotData;
import com.acornworks.projectset.domains.StockPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Component
public class HistoricalDataProcessor {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String spotBaseUrl;
    private String historicalBaseUrl;
    private RestTemplate restTemplate;

    public HistoricalDataProcessor(
        RestTemplate restTemplate, 
        @Value("${yahoo.url.spot}") String spotBaseUrl,
        @Value("${yahoo.url.historical}") String historicalBaseUrl) {
        this.restTemplate = restTemplate;
        this.spotBaseUrl = spotBaseUrl;
        this.historicalBaseUrl = historicalBaseUrl;
    }

    

    public SpotData getPrice(String symbol) throws NullPointerException {
        logger.info("Get price from Yahoo for {}", symbol);
        final String url = String.format(spotBaseUrl, symbol);
        logger.info("Calling URL: {}", url);

        final JsonNode rootNode = restTemplate.getForObject(url, JsonNode.class);

        if (rootNode == null) {
            throw new NullPointerException(url);
        }

        final JsonNode resultNode = rootNode.get("optionChain").get("result").get(0);

        final BigDecimal price = BigDecimal.valueOf(resultNode.get("quote").get("regularMarketPrice").asDouble());
        final Calendar timestamp = Calendar.getInstance();

        return new SpotData(symbol, price, timestamp);
    }

    public List<StockPrice> getHisoricalPrice(String symbol) throws IOException, CsvException, ParseException {
        logger.info("Get historical prices for an year for {}", symbol);

        final Calendar cal = Calendar.getInstance();
        final long endMill = (long)(cal.toInstant().toEpochMilli() / 1000L);

        cal.add(Calendar.YEAR, -1);
        
        final long startMill = (long)(cal.toInstant().toEpochMilli() / 1000L);
        final String url = String.format(historicalBaseUrl, symbol, startMill, endMill);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        logger.info("Calling URL: {}", url);

        final String priceCsvStr = restTemplate.getForObject(url, String.class);
        final List<StockPrice> returnList = new ArrayList<>();

        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try(CSVReader reader = new CSVReader(new StringReader(priceCsvStr))) {
            final List<String[]> readLines = reader.readAll();
            
            for (final String[] lineStrs : readLines) {
                if (lineStrs[0].equals("Date")) {
                    continue;
                }

                final Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(formatter.parse(lineStrs[0]));

                returnList.add(new StockPrice(
                    symbol, 
                    dateCal, 
                    new BigDecimal(lineStrs[1]), 
                    new BigDecimal(lineStrs[2]), 
                    new BigDecimal(lineStrs[3]), 
                    new BigDecimal(lineStrs[4]), 
                    new BigDecimal(lineStrs[6]))
                );
            }
        }    

        return returnList;
    }
}
