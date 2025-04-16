package com.capstone.landlordInsurance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fire_water_coverage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FireWaterCov {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fireWaterCovId;

    private String constructionType;
    private String proximityToFireStation;
    private String ageOfPlumbingSystem;
    private String plumbingCondition;
    private boolean isInFloodProneArea;

    private boolean hasSmokeDetectors;
    private boolean hasFireAlarms;
    private boolean hasFireExtinguishers;
    private boolean hasSprinklerSystem;

    @OneToOne
    @JoinColumn(name = "quote_id", nullable = false)
    @JsonIgnore
    private Quote quote;

}
