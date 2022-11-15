package com.acornworks.projectset.processors;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acornworks.projectset.common.Payload;
import com.acornworks.projectset.domains.SpotData;
import com.acornworks.projectset.domains.StockPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class HistoricalDataProcessorTests {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HistoricalDataProcessor historicalDataProcessor;

    @Test
    void testGetPrice() throws IOException {
        final JsonNode payloadNode = Payload.readPayload("payloads/audkrw.json");
        ReflectionTestUtils.setField(historicalDataProcessor, "spotBaseUrl", "https://localhost:60081/%s");

        Mockito.when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                Mockito.eq(JsonNode.class))).thenReturn(payloadNode);

        final SpotData spotData = historicalDataProcessor.getPrice("AUDKRW=X");

        Assertions.assertEquals(931.43, spotData.getPrice().doubleValue());
        Assertions.assertEquals("AUDKRW=X", spotData.getTicker());
        Assertions.assertNotNull(spotData.getTimestamp());
    }    

    @Test
    void testGetHistoricalPrice() throws Exception {
        final String payloadCsvStr = Payload.readPayloadString("payloads/AAPL.csv");
        
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        ReflectionTestUtils.setField(historicalDataProcessor, "historicalBaseUrl", "https://localhost:60081/%s/%d/%d");

        Mockito.when(restTemplate.getForObject(
            ArgumentMatchers.anyString(),
            Mockito.eq(String.class))).thenReturn(payloadCsvStr);
        
        final List<StockPrice> historicalPrices = historicalDataProcessor.getHisoricalPrice("AAPL");

        Assertions.assertEquals(252, historicalPrices.size());

        final StockPrice startPrice = historicalPrices.get(0);

        Assertions.assertEquals("2021-11-11", formatter.format(startPrice.getDate().getTime()));
        Assertions.assertEquals("148.960007", startPrice.getOpen().toString());
        Assertions.assertEquals("149.429993", startPrice.getHigh().toString());
        Assertions.assertEquals("147.679993", startPrice.getLow().toString());
        Assertions.assertEquals("147.869995", startPrice.getClose().toString());
        Assertions.assertEquals("41000000", startPrice.getVolume().toString());

        Assertions.assertEquals("2022-11-10", formatter.format(historicalPrices.get(251).getDate().getTime()));

    }
}
