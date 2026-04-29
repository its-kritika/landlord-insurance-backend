package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import com.capstone.landlordInsurance.service.BrokerDetailsServiceImpl;
import com.capstone.landlordInsurance.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    BrokerDetailsServiceImpl brokerDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //handling frontend request when user clicks google login button.
    //It only starts the Google login flow by redirecting the browser to Google.
    //Telling Google, after login, send me an authorization code
    @GetMapping
    public void handleGoogleLogin(HttpServletResponse response) throws IOException {

        String googleAuthUrl =
                "https://accounts.google.com/o/oauth2/v2/auth"
                        + "?client_id=" + clientId
                        + "&redirect_uri=" + URLEncoder.encode("http://localhost:8080/auth/google/callback", StandardCharsets.UTF_8)
                        + "&response_type=code"
                        + "&scope=" + URLEncoder.encode("email profile", StandardCharsets.UTF_8);

        response.sendRedirect(googleAuthUrl);
    };

    //output returned by google (authorization code taken from google and a token is generated)
    //authorization code approach is more secure that directly taking token from google
    //Google sends the authorization code to this method
    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code){ // code = Authorization code from google
        try{
            // Exchange auth code for tokens
            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", "http://localhost:8080/auth/google/callback");
//            params.add("redirect_uri", "https://developers.google.com/oauthplayground");  //http://localhost:8080/auth/google/callback
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //key1=value1&key=value2&... (key may repeat that's why using MultiValueMap else Hashmap can be used)

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            String idToken = (String) tokenResponse.getBody().get("id_token");
            String brokerInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> brokerInfoResponse = restTemplate.getForEntity(brokerInfoUrl, Map.class);

            if (brokerInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> brokerGoogleInfo = brokerInfoResponse.getBody();
                String email = (String) brokerGoogleInfo.get("email");
                String googleName = (String) brokerGoogleInfo.get("name");
                UserDetails brokerDetails = null;

                Broker savedBroker;

                try{
                    brokerDetails = brokerDetailsService.loadUserByUsername(email);
                    savedBroker = brokerRepository.findByEmail(email);

                }catch (Exception e){
                    Broker broker = new Broker();
                    broker.setEmail(email);
                    broker.setName(googleName);

                    //generating random password for google login
                    broker.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    savedBroker = brokerRepository.save(broker);
                }
                String jwtToken = jwtUtils.generateToken(email);

                //Redirecting back to frontend with token and name in url
                String redirectUrl = "http://localhost:4200/auth/callback?token=" + jwtToken
                        + "&name=" + URLEncoder.encode(savedBroker.getName(), StandardCharsets.UTF_8);

                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(redirectUrl))
                        .build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Exception occurred while handleGoogleCallback ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
