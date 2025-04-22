package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.ClientDto;
import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.Client;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.service.BrokerService;
import com.capstone.landlordInsurance.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private BrokerService brokerService;

    @PostMapping("add-client")
    public ResponseEntity<?> createClient(@RequestBody ClientDto newClient){
        Map<String, String> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();

            Client client = clientService.saveClient(newClient, brokerEmail);
            response.put("message", "Client created successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            response.put("error", "Client with this email already exists");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("error", "Something went wrong");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Client client = clientService.getClientById(id);
            if (client != null) {
                return new ResponseEntity<>(client, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-clients")
    public ResponseEntity<?> getClientsByBroker() {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            Broker broker = brokerService.findByEmail(brokerEmail);
            if (broker == null) {
                throw new RuntimeException("Broker not found");
            }

            Long brokerId = broker.getBrokerId();
            List<Client> clients;
            clients = clientService.getClientByBrokerId(brokerId);

            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error in fetching clients", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody ClientDto updatedClient) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            Client client = clientService.updateClientById(id, updatedClient);
            return new ResponseEntity<>("Client updated successfully!", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(Exception e){
            return new ResponseEntity<>("Error in updating client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean deleted = clientService.deleteClientById(id);
            if (deleted) {
                return new ResponseEntity<>("Client deleted successfully!", HttpStatus.OK);
            }
            return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error in deleting client", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
