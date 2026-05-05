package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.service.BrokerDetailsServiceImpl;
import com.capstone.landlordInsurance.service.BrokerService;
import com.capstone.landlordInsurance.utils.JwtUtils;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private BrokerDetailsServiceImpl brokerDetailsService;

    @PostMapping("signup")
    public ResponseEntity<?> createBroker(@RequestBody Broker broker){
        Map<String, String> response = new HashMap<>();
        try{
            Broker savedBroker = brokerService.saveBroker(broker);  // function created in service package

            String jwt = jwtUtils.generateToken(broker.getEmail());
            response.put("token", jwt);
            response.put("name", savedBroker.getName());
            response.put("email", broker.getEmail());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (DataIntegrityViolationException e) {
            response.put("error", "Broker already exists. Please Login!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException e) {
            response.put("error", "All fields are mandatory!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e){

            response.put("error", "Something went wrong. Please try again!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Broker broker){
        Map<String, String> response = new HashMap<>();
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(broker.getEmail(), broker.getPassword()));
            UserDetails brokerDetails = brokerDetailsService.loadUserByUsername(broker.getEmail());
            String jwt = jwtUtils.generateToken(brokerDetails.getUsername());
            Broker savedBroker = brokerService.findByEmail(broker.getEmail());

            response.put("token", jwt);
            response.put("name", savedBroker.getName());
            response.put("email", broker.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e){
            response.put("error", "Incorrect email or password!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateBroker(@RequestBody Broker updatedBroker) {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            Broker findBroker = brokerService.findByEmail(brokerEmail);
            if (findBroker == null){
                throw new RuntimeException("Broker not found");
            }

            Long id = findBroker.getBrokerId();
            brokerService.updateBroker(id, updatedBroker);
            response.put("message", "Profile updated successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.put("error", "Error in updating profile");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request){
        Map<String, String> response = new HashMap<>();

        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            String brokerEmail = jwtUtils.extractUsername(token);

            if (brokerEmail == null || brokerEmail.isBlank()) {
                throw new RuntimeException("Invalid request!");
            }

            brokerService.updateBrokerPassword(brokerEmail, newPassword);

            response.put("message", "Password updated successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            response.put("error", "Failed to update password. Please try again!");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete Broker
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteBroker(@PathVariable Long id) {
//        boolean deleted = brokerService.deleteBroker(id);
//        if (deleted) {
//            return new ResponseEntity<>("Broker deleted successfully", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Broker not found", HttpStatus.NOT_FOUND);
//        }
//    }
}
