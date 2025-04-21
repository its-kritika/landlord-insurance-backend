package com.capstone.landlordInsurance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "income_loss_coverage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LossOfIncomeCov {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IncomeLossCovId;

    private double monthlyRentalIncome;
    private int numberOfTenants;
    private boolean hasMortgage;

    private int vacantDaysPastYear;
    private int unitsRented;

    // This field can be computed in backend or kept nullable if calculated later
    private Double vacancyRate;

    @OneToOne
    @JoinColumn(name = "quote_id", nullable = false)
    @JsonIgnore
    private Quote quote;

}
