package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.Client;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import com.capstone.landlordInsurance.repository.ClientRepository;
import com.capstone.landlordInsurance.repository.QuoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BrokerService {
    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    public void saveBroker(Broker broker){
        brokerRepository.save(broker);
    }

    @Transactional
    public Broker updateBroker(Long id, Broker updatedBroker) {
        return brokerRepository.findById(id)
                .map(existingBroker -> {
                    if (updatedBroker.getEmail() != null && !existingBroker.getEmail().equals(updatedBroker.getEmail())) {
                        throw new IllegalArgumentException("Email address cannot be updated.");
                    }
                    if (updatedBroker.getName() != null) {
                        existingBroker.setName(updatedBroker.getName());
                    }
                    if (updatedBroker.getPassword() != null) {
                        existingBroker.setPassword(updatedBroker.getPassword());
                    }

                    return brokerRepository.save(existingBroker);
                })
                .orElseThrow(() -> new NoSuchElementException("Broker not found with ID: " + id));
    }
}
