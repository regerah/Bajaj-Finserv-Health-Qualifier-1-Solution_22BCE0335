package com.example.bajaj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class StartupService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // API URLs
    private static final String WEBHOOK_GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String WEBHOOK_SUBMIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
    
    // SQL Query - Find highest salary not credited on 1st day of month
    private static final String SQL_QUERY = 
        "SELECT p.AMOUNT AS SALARY, " +
        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
        "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
        "d.DEPARTMENT_NAME " +
        "FROM PAYMENTS p " +
        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
        "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
        "ORDER BY p.AMOUNT DESC " +
        "LIMIT 1;";

    public StartupService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Bajaj Finserv application startup process...");
        
        try {
            // Step 1: Generate webhook and get access token
            WebhookResponse webhookResponse = generateWebhook();
            logger.info("Webhook generated successfully");
            
            // Step 2: Submit SQL query to webhook
            submitSqlQuery(webhookResponse.getAccessToken());
            logger.info("SQL query submitted successfully");
            
            logger.info("Startup process completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error during startup process: {}", e.getMessage(), e);
            throw e;
        }
    }

    private WebhookResponse generateWebhook() throws Exception {
        logger.info("Generating webhook...");
        
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create HTTP entity
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Make POST request
        ResponseEntity<String> response = restTemplate.postForEntity(
            WEBHOOK_GENERATE_URL, 
            requestEntity, 
            String.class
        );
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to generate webhook. Status: " + response.getStatusCode());
        }
        
        // Parse response
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String webhook = responseJson.get("webhook").asText();
        String accessToken = responseJson.get("accessToken").asText();
        
        logger.info("Webhook URL: {}", webhook);
        logger.info("Access Token received: {}...", accessToken.substring(0, Math.min(10, accessToken.length())));
        
        return new WebhookResponse(webhook, accessToken);
    }

    private void submitSqlQuery(String accessToken) throws Exception {
        logger.info("Submitting SQL query to webhook...");
        
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("finalQuery", SQL_QUERY);
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        // Create HTTP entity
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Make POST request to the correct webhook submission URL
        ResponseEntity<String> response = restTemplate.postForEntity(
            WEBHOOK_SUBMIT_URL, 
            requestEntity, 
            String.class
        );
        
        logger.info("SQL query submission response status: {}", response.getStatusCode());
        logger.info("SQL query submission response body: {}", response.getBody());
        
        if (response.getStatusCode() != HttpStatus.OK) {
            logger.warn("Unexpected response status: {}", response.getStatusCode());
        }
    }

    // Inner class to hold webhook response data
    private static class WebhookResponse {
        private final String accessToken;

        public WebhookResponse(String webhook, String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
