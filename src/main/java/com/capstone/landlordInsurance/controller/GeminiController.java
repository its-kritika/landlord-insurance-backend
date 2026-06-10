package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("send")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {

        Map<String, String> response = new HashMap<>();

        try{
            String message = request.get("message");
            String answer = geminiService.askQuestion(message);

            response.put("reply", answer);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {

            String errorMessage = e.getMessage();

            if (errorMessage != null &&
                    (errorMessage.contains("RESOURCE_EXHAUSTED")
                            || errorMessage.contains("429")
                            || errorMessage.contains("Quota exceeded"))) {

                response.put(
                        "error",
                        "AI service limit reached. Try again later."
                );

                return new ResponseEntity<>(
                        response,
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }

            if (errorMessage != null &&
                    (errorMessage.contains("503")
                    || errorMessage.contains("UNAVAILABLE")
                    || errorMessage.contains("high demand"))) {

                response.put(
                        "error",
                        "AI service is currently busy. Please try again in a few minutes."
                );

                return new ResponseEntity<>(
                        response,
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            response.put(
                    "error",
                    "Something went wrong while generating a response."
            );

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
