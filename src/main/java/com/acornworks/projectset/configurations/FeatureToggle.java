package com.acornworks.projectset.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class FeatureToggle {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RestTemplate restTemplate;
    private String serverUrl;

    public FeatureToggle(RestTemplate restTemplate, @Value("${featuretoggle.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public boolean getFeature(final String featureName) {
        final String callUrl = String.format("%s/api/v1/flags/%s", serverUrl, featureName);

        logger.info("Toogle Call URL: {}", callUrl);

        final JsonNode rootNode = restTemplate.getForObject(callUrl, JsonNode.class);

        if (rootNode == null) {
            return false;
        }

        final JsonNode enabledNode = rootNode.get("enabled");

        return enabledNode == null ? false : enabledNode.asBoolean();

    }
}
