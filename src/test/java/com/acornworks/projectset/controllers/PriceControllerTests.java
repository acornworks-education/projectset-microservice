package com.acornworks.projectset.controllers;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.acornworks.projectset.common.Payload;
import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.SpotData;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.processors.HistoricalDataProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = PriceController.class)
public class PriceControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoricalDataProcessor processor;

    @MockBean
    private FeatureToggle featureToggle;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetPrice() throws Exception {
        final Calendar calendar = Calendar.getInstance();

        Mockito.when(processor.getPrice(Mockito.anyString()))
                .thenReturn(new SpotData("AUDKRW=X", new BigDecimal("900.00"), calendar));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/price/spot/AUDKRW=X")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        final JsonNode resultNode = objectMapper.readTree(result.getResponse().getContentAsString());

        Assertions.assertEquals("AUDKRW=X", resultNode.get("ticker").asText());
        Assertions.assertEquals(900.0d, resultNode.get("price").asDouble());
        Assertions.assertNotNull(resultNode.get("timestamp"));
    }

    @Test
    void testGetHistoricalPrices() throws Exception {
        List<StockPrice> mockPrices = Payload.readHistoricalDataPayload("AAPL", "payloads/AAPL.csv");

        Mockito.when(processor.getHisoricalPrice(Mockito.anyString()))
                .thenReturn(mockPrices);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/price/historical/AUDKRW=X")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        final JsonNode resultNode = objectMapper.readTree(result.getResponse().getContentAsString());

        Assertions.assertEquals(252, resultNode.size());
        Assertions.assertEquals("2021-11-11", resultNode.get(0).get("date").asText());
        Assertions.assertEquals("2022-11-10", resultNode.get(251).get("date").asText());
    }
}
