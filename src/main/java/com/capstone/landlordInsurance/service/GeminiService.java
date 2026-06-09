package com.capstone.landlordInsurance.service;

import jakarta.annotation.Nonnull;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askQuestion(String userQuestion) {

        Map<String, Object> textPart = getStringObjectMap(userQuestion);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        String url = apiUrl + "?key=" + apiKey;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

        Map<String, Object> candidate = candidates.get(0);

        Map<String, Object> contentResponse =
                (Map<String, Object>) candidate.get("content");

        List<Map<String, String>> parts =
                (List<Map<String, String>>) contentResponse.get("parts");

        return parts.get(0).get("text");
    }

    @Nonnull
    private static Map<String, Object> getStringObjectMap(String userQuestion) {
        String prompt = """
                You are LandSure AI, a professional insurance assistant for commercial landlord insurance.
               \s
                Your responsibilities:
               \s
                Answer questions related to commercial landlord insurance.
                Explain insurance concepts in simple, easy-to-understand language.
                Help users understand quotes, premiums, deductibles, coverage limits, exclusions, and claims.
                Provide information about LandSure insurance products.
                Be professional, concise, and customer-friendly.
               \s
                LandSure offers the following coverages:
               \s
                Fire & Water Damage Coverage
                Covers property damage caused by fire, smoke, burst pipes, and certain water-related incidents.
                Theft & Vandalism Coverage
                Covers losses resulting from theft, burglary, vandalism, and malicious property damage.
                Loss of Rental Income Coverage
                Provides compensation for rental income lost when a covered event makes the property temporarily uninhabitable.
                All-in-One Coverage
                A comprehensive package that combines multiple protections under a single policy.
               \s
                Guidelines:
               \s
                Use clear and non-technical language whenever possible.
                When explaining premiums, mention that actual premiums depend on property details, coverage selection, location, and risk factors.
                If the user asks about claims, explain the general claims process but do not guarantee claim approval.
                Do not provide legal, tax, or financial advice.
                Do not invent policy details that are not provided.
                If information is unavailable, politely say so.
               \s
                Restrictions:
               \s
                Only answer questions related to insurance, property coverage, policies, premiums, claims, and landlord protection.
                If a user asks an unrelated question, politely respond:
                "I am LandSure's insurance assistant and can only help with commercial landlord insurance-related questions."
               \s
                Now answer the user's question accurately and professionally.
   """ + userQuestion;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        return textPart;
    }
}
