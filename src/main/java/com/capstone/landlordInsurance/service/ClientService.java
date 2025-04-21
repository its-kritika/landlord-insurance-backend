package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.ClientDto;
import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.Client;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import com.capstone.landlordInsurance.repository.ClientRepository;
import com.capstone.landlordInsurance.repository.PremiumRepository;
import com.capstone.landlordInsurance.repository.QuoteRepository;
import com.capstone.landlordInsurance.utils.ClientUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PremiumRepository premiumRepository;

    @Transactional
    public Client saveClient(ClientDto clientDto){
        Broker broker = brokerRepository.findById(clientDto.getBrokerId())
                .orElseThrow(() -> new IllegalArgumentException("Broker not found"));

        // Create and populate Client
        Client client = new Client();
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setAddress(clientDto.getAddress());
        client.setPhone(clientDto.getPhone());
        client.setBroker(broker); // Set the broker

        // Save the Client
        return clientRepository.save(client);
    }

    public List<Client> getClientByNameAndId(Long brokerId, String query){
        return clientRepository.findByBroker_BrokerIdAndNameContainingIgnoreCase(brokerId, query);
    }

    public List<Client> getClientByBrokerId(Long brokerId){
        return clientRepository.findTop5ByBroker_BrokerIdOrderByCreatedAtDesc(brokerId); //findByBroker_BrokerId
    }

    @Transactional
    public Client updateClientById(Long id, ClientDto updatedClient) {
        return clientRepository.findById(id)
                .map(existingClient -> {
                    if (updatedClient.getEmail() != null && !existingClient.getEmail().equals(updatedClient.getEmail())) {
                        throw new IllegalArgumentException("Email address cannot be updated.");
                    }

                    ClientUtils.updateClientFields(existingClient, updatedClient);

                    return clientRepository.save(existingClient);
                })
                .orElseThrow(() -> new NoSuchElementException("Client not found with ID: " + id));
    }

    // delete all quotes related to the client when client is deleted
//    @Transactional
//    public boolean deleteClientById(Long id) {
//        Optional<Client> optionalClient = clientRepository.findById(id);
//        if (optionalClient.isPresent()) {
//            Client client = optionalClient.get();
//
//            List<Quote> quotes = quoteRepository.findByClient_ClientId(client.getClientId());
////            for (Quote quote : quotes) {
////                quote.setFireWaterCoverage(null);
////                quote.setVandalismTheftCoverage(null);
////                quote.setLossOfIncomeCoverage(null);
////                quote.setBroker(null);  // unlink broker to avoid FK violation
////            }
//            quoteRepository.deleteAll(quotes);
//
//            clientRepository.deleteById(id);
//            return true;
//        }
//        return false;
//    }

    @Transactional
    public boolean deleteClientById(Long id) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            return false;
        }

        Client client = optionalClient.get();

        // Step 1: Fetch and delete all quotes associated with this client
        List<Quote> quotes = quoteRepository.findByClient_ClientId(client.getClientId());

        List<Long> quoteIds = quotes.stream()
                .map(Quote::getQuoteId)
                .collect(Collectors.toList());
        premiumRepository.deleteAllByQuoteIds(quoteIds);

        for (Quote quote : quotes) {
            // Break circular references to avoid constraint issues
            if (quote.getFireWaterCoverage() != null) {
                quote.getFireWaterCoverage().setQuote(null);
            }
            if (quote.getVandalismTheftCoverage() != null) {
                quote.getVandalismTheftCoverage().setQuote(null);
            }
            if (quote.getLossOfIncomeCoverage() != null) {
                quote.getLossOfIncomeCoverage().setQuote(null);
            }
            quote.setClient(null);  // Break client reference
            quote.setBroker(null);  // Break broker reference (optional safety)
        }
        quoteRepository.saveAll(quotes);
        quoteRepository.deleteAll(quotes);

        // Step 2: Delete the client
        clientRepository.delete(client);

        return true;
    }

}
