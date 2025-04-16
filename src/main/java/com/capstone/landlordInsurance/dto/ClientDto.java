package com.capstone.landlordInsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDto {
    private String name;
    private String email;
    private String address;
    private String phone;
    private Long brokerId; // Broker ID from the JSON

}
