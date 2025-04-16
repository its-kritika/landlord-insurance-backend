package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @PostMapping("add-broker")
    public ResponseEntity<Broker> createBroker(@RequestBody Broker broker){
        try{
            brokerService.saveBroker(broker);  // function created in service package
            return new ResponseEntity<>(broker, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBroker(@PathVariable Long id, @RequestBody Broker updatedBroker) {
        try {
            Broker broker = brokerService.updateBroker(id, updatedBroker);
            return new ResponseEntity<>(broker, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
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
