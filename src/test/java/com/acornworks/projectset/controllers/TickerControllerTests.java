package com.acornworks.projectset.controllers;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.Ticker;
import com.acornworks.projectset.repositories.TickerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
@WebMvcTest(value = TickerController.class)
public class TickerControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TickerRepository tickerRepository;

    @MockBean
    private FeatureToggle featureToggle;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetTickerList() throws Exception {
        final List<Ticker> mockDataTickers = Arrays.asList(
            new Ticker("AUDKRW=X", "FX AUD/KRW"),
            new Ticker("AUDEUR=X", "FX AUD/EUR"),
            new Ticker("AUDUSD=X", "FX AUD/USD")
        );

        Mockito.when(tickerRepository.findAll()).thenReturn(mockDataTickers);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/ticker/list").accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        final String contentString = result.getResponse().getContentAsString();
        final List<Ticker> tickerList = objectMapper.readValue(contentString, new TypeReference<List<Ticker>>() {});

        Assertions.assertEquals(mockDataTickers.size(), tickerList.size());

        for (int i = 0; i < mockDataTickers.size(); i++) {
            Assertions.assertEquals(mockDataTickers.get(i), tickerList.get(i));
        }
    }

    @Test
    public void testAddTicker() throws Exception {
        final String validJsonStr = "{\"ticker\": \"AUDKRW=X\", \"name\": \"FX AUD/KRW\"}";

        final List<String> invaildCases = Arrays.asList(
            "{}",
            "{\"ticker\": \"AUDKRW=X\"}",
            "{\"name\": \"FX AUD/KRW\"}"
        );

        for (final String jsonStr : invaildCases) {
            RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/ticker")
                .content(jsonStr).accept(MediaType.APPLICATION_JSON);

            try {
                mockMvc.perform(requestBuilder);
            } catch(Exception ex) {
                Assertions.assertTrue(ex.getMessage().indexOf("KEY_TICKER_NAME_SHOULD_BE_DEFINED") >= 0);
            }    
        }

        Mockito.when(tickerRepository.save(any())).thenReturn(new Ticker("AUDKRW=X", "FX AUD/KRW"));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/ticker")
        .content(validJsonStr).accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());

        final String contentString = result.getResponse().getContentAsString();
        final JsonNode resultNode = objectMapper.readTree(contentString);

        Assertions.assertEquals("AUDKRW=X", resultNode.get("ticker").asText());
        Assertions.assertEquals("FX AUD/KRW", resultNode.get("name").asText());
    }
    
    @Test
    public void testRemoveTicker() throws Exception {
        final String validJsonStr = "{\"ticker\": \"AUDKRW=X\"}";

        final List<String> invaildCases = Arrays.asList(
            "{}",
            "{\"name\": \"FX AUD/KRW\"}"
        );

        for (final String jsonStr : invaildCases) {
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/ticker")
                .content(jsonStr).accept(MediaType.APPLICATION_JSON);

            try {
                mockMvc.perform(requestBuilder);
            } catch(Exception ex) {
                Assertions.assertTrue(ex.getMessage().indexOf("KEY_TICKER_SHOULD_BE_DEFINED") >= 0);
            }    
        }

        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/ticker")
        .content(validJsonStr).accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());

        final String contentString = result.getResponse().getContentAsString();
        final JsonNode resultNode = objectMapper.readTree(contentString);

        Assertions.assertEquals("AUDKRW=X", resultNode.get("ticker").asText());
        Assertions.assertEquals("", resultNode.get("name").asText());
    }
}
