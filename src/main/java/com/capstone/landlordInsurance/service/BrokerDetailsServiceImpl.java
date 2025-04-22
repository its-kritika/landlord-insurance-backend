package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BrokerDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private BrokerRepository brokerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Broker broker = brokerRepository.findByEmail(email);
        if (broker != null){

            return org.springframework.security.core.userdetails.User.builder()
                    .username(broker.getEmail())
                    .password(broker.getPassword())
                    .build();
        }
        throw new UsernameNotFoundException("User not found with email id " + email);

    }
}
