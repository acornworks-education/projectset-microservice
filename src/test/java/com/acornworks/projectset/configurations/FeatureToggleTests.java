package com.acornworks.projectset.configurations;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
public class FeatureToggleTests {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FeatureToggle featureToggle;

    @Test
    void testGetFeature() throws Exception {
        ReflectionTestUtils.setField(featureToggle, "serverUrl", "http://localhost:64080");

        Mockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), Mockito.eq(JsonNode.class)))
        .thenReturn(null);

        boolean result = featureToggle.getFeature("dummy");

        Assertions.assertFalse(result);

        JsonNode payloadNode = objectMapper.readTree("{\"enabled\": true}");

        Mockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), Mockito.eq(JsonNode.class)))
        .thenReturn(payloadNode);

        result = featureToggle.getFeature("dummy");

        Assertions.assertTrue(result);

        Mockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), Mockito.eq(JsonNode.class)))
        .thenReturn(null);

        result = featureToggle.getFeature("dummy");

        Assertions.assertFalse(result);

        payloadNode = objectMapper.readTree("{\"dummy\": true}");

        Mockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), Mockito.eq(JsonNode.class)))
        .thenReturn(payloadNode);

        result = featureToggle.getFeature("dummy");

        Assertions.assertFalse(result);
    }

    
}
