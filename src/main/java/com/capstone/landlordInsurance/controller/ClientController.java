package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.ClientDto;
import com.capstone.landlordInsurance.entity.Client;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("add-client")
    public ResponseEntity<Client> createClient(@RequestBody ClientDto newClient){
        try{
            Client client = clientService.saveClient(newClient);  // function created in service package
            return new ResponseEntity<>(client, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        if (client != null) {
            return new ResponseEntity<>(client, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get-clients")
    public ResponseEntity<List<Client>> getClientsByBroker(
            @RequestParam Long brokerId,
            @RequestParam(required = false) String query) {

        List<Client> clients;

        if (query != null && !query.isEmpty()) {
            //fetch by brokerId and query (?brokerId=1?name=amrita)
            clients = clientService.getClientByNameAndId(brokerId, query);
        } else {
            //fetch by brokerId only when query is not given (?brokerId=1)
            clients = clientService.getClientByBrokerId(brokerId);
        }

        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody ClientDto updatedClient) {
        try {
            Client client = clientService.updateClientById(id, updatedClient);
            return new ResponseEntity<>(client, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        boolean deleted = clientService.deleteClientById(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
