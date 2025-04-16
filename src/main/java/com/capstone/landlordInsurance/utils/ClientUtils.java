package com.capstone.landlordInsurance.utils;

import com.capstone.landlordInsurance.dto.ClientDto;
import com.capstone.landlordInsurance.entity.Client;

public class ClientUtils {

    public static void updateClientFields(Client existingClient, ClientDto updatedClient) {
        if (updatedClient.getName() != null) {
            existingClient.setName(updatedClient.getName());
        }
        if (updatedClient.getPhone() != null) {
            existingClient.setPhone(updatedClient.getPhone());
        }
        if (updatedClient.getAddress() != null) {
            existingClient.setAddress(updatedClient.getAddress());
        }
    }
}
