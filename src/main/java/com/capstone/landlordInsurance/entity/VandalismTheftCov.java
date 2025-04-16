package com.capstone.landlordInsurance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vandalism_theft_coverage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VandalismTheftCov {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vandalismTheftCovId;

    private boolean hasCCTV;
    private boolean hasAlarms;
    private boolean hasGatedAccess;
    private boolean hasSmartLock;

    private boolean hadPreviousIncidents;

    private String neighCrimeRate;

    @OneToOne
    @JoinColumn(name = "quote_id", nullable = false)
    @JsonIgnore
    private Quote quote;

}
