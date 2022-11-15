package com.acornworks.projectset.controllers;

import static org.mockito.ArgumentMatchers.any;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.acornworks.projectset.common.Payload;
import com.acornworks.projectset.configurations.FeatureToggle;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.domains.TradingResult;
import com.acornworks.projectset.processors.HistoricalDataProcessor;
import com.acornworks.projectset.processors.StrategyProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = StrategyController.class)
public class StrategyControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoricalDataProcessor historicalDataProcessor;

    @MockBean
    private StrategyProcessor strategyProcessor;

    @MockBean
    private FeatureToggle featureToggle;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetAnalysis() throws Exception {
        List<StockPrice> mockPrices = Payload.readHistoricalDataPayload("AAPL", "payloads/AAPL.csv");

        Mockito.when(historicalDataProcessor.getHisoricalPrice(Mockito.anyString()))
                .thenReturn(mockPrices);
        
        Mockito.when(strategyProcessor.getAnalysisResult(any(), any())).thenCallRealMethod();
        Mockito.when(strategyProcessor.getBarSeriesFromStockData(any())).thenCallRealMethod();
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/strategy/AAPL/ADX")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        final TradingResult expectedResult = objectMapper.readValue(
            Payload.readPayloadString("payloads/adx-strategy.json"), TradingResult.class);
        
        final TradingResult actualResult = objectMapper.readValue(
            result.getResponse().getContentAsString(), TradingResult.class);

        Assertions.assertEquals(expectedResult, actualResult);
    }
    
}
