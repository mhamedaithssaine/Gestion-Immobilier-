package com.example.gestionimmobilier.models.entity.immobilier;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name =  "adresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String rue;

    @Column(nullable = false)
    private String ville;

    @Column(name = "code_postal", nullable = false)
    private String codePostal;

    @Column(nullable = false)
    private String pays;
}
